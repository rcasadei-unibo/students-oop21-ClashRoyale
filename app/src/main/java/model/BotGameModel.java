package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.badlogic.gdx.math.Vector2;

import model.entities.Attackable;
import model.entities.cards.Card;
import model.entities.towers.KingTower;
import model.entities.towers.QueenTower;
import model.entities.towers.Tower;
import model.entities.users.Bot;
import model.entities.users.User;
import utilities.VectorsUtilities;

/**
 * An implementation of GameController in which the user plays 
 * against a bot.
 */
public class BotGameModel extends GameModel {

  private final List<Card> botCards;
  private final List<Card> botCardQueue;
  private final List<Card> botDeployedCards;
  private final List<Card> botChoosableCards;
  private final List<Tower> botActiveTowers;
 
  /**
   * 
   * @param playerCards
   *              the player deck.
   * @param botCards
   *              {@inheritDoc}.
   */
  public BotGameModel(final List<Card> playerCards, final List<Card> botCards, final User player, final Bot bot) {
    super(playerCards, player);
    this.botCards = botCards.stream().collect(Collectors.toList());
    this.botCardQueue = botCards.stream().collect(Collectors.toList());
    this.botDeployedCards = new ArrayList<>();
    this.botChoosableCards = new ArrayList<>();
    IntStream.range(0, GameModel.CHOOSABLE_CARDS).forEach(i -> this.botChoosableCards.add(this.botCardQueue.remove(0)));
    this.botActiveTowers = this.getBotTowers(bot);
  }

  private List<Tower> getBotTowers(final Bot bot) {
    final List<Tower> towers = new ArrayList<>();
    final var leftTowerPosition = new Vector2(238, 657);
    final var rightTowerPosition = new Vector2(448, 657);
    final var centralTowerPosition = new Vector2(344, 706);
    towers.add(QueenTower.create(bot, leftTowerPosition));
    towers.add(QueenTower.create(bot, rightTowerPosition));
    towers.add(KingTower.create(bot, centralTowerPosition));
    return towers;
  }

  /**
   * 
   * @return a list of every card used from the bot during the match.
   */
  public List<Card> getBotDeck() {
    return Collections.unmodifiableList(this.botCards);
  }

  /**
   * 
   * @return the queued cards of the bot.
   */
  public List<Card> getPBotCardQueue() {
    return Collections.unmodifiableList(this.botCardQueue);
  }

  /**
   * 
   * @return a list of bot currently deployed cards.
   */
  public List<Card> getBotDeployedCards() {
    return Collections.unmodifiableList(this.botDeployedCards);
  }

  /**
   * 
   * @return a list of bot currently choosable cards.
   */
  public List<Card> getBotChoosableCards() {
    return Collections.unmodifiableList(this.botChoosableCards);
  }

  /**
   * Deploys a card of the bot.
   * @param card
   *           the card to be deployed.
   */
  public void deployBotCard(final Card card) {
    if (this.botChoosableCards.contains(card)) {
      this.botChoosableCards.remove(card);
      this.botCardQueue.add(card);
      this.botDeployedCards.add(card);
    }
  }

  /**
   * @param origin 
                  the start position of the new card.
   * @return an {@link Optional} of the first card entered in the queue.
   * 
   */
  public Optional<Card> getBotNextQueuedCard(final Vector2 origin) {
    if (this.botCardQueue.isEmpty()) {
      return Optional.empty();
    }
    final var nextCard = this.botCardQueue.remove(0).createAnother(origin);
    this.botChoosableCards.add(nextCard);
    return Optional.of(nextCard);
  }

  /**
   * Removes a card from the map.
   * @param card
   *           the card to be removed.
   */
  public void removeBotCardFromMap(final Card card) {
    if (this.botDeployedCards.contains(card)) {
      this.botDeployedCards.remove(card);
    }
  }

  /**
   * 
   * @return the currently active towers of the bot.
   */
  public List<Tower> getBotActiveTowers() {
    return Collections.unmodifiableList(this.botActiveTowers);
  }

  /**
   * If not already, destroys a bot tower.
   * 
   * @param tower
   *            the tower to be destroyed.
   */
  public void destroyBotTower(final Tower tower) {
    if (this.botActiveTowers.contains(tower)) {
      this.botActiveTowers.remove(tower);
    }
  }

  /**
   * 
   * @return a list of attackable elements of the bot.
   */
  public List<Attackable> getBotAttackable() {
    return Stream.concat(this.botDeployedCards.stream().map(c -> (Attackable) c), this.botActiveTowers.stream().map(t -> (Attackable) t)).collect(Collectors.toList());
  }

  private void findTargets(final List<Attackable> selfAttackables, final List<Attackable> enemyAttackables) {
    selfAttackables
      .stream()
      .filter(selfAttackable -> selfAttackable.getCurrentTarget().isEmpty())
      .forEach(selfAttackable -> enemyAttackables
          .stream()
          .filter(enemyAttackable -> this.isInRange(selfAttackable, enemyAttackable))
          .findAny()
          .ifPresent(enemyAttackable -> selfAttackable.setCurrentTarget(enemyAttackable)));
  }

  private boolean isInRange(final Attackable selfAttackable, final Attackable enemyAttackable) {
    return VectorsUtilities.euclideanDistance(selfAttackable.getPosition(), enemyAttackable.getPosition()) <= selfAttackable.getRange();
  }

  @Override
  public void findAttackableTargets() {
    this.findTargets(super.getPlayerAttackable(), this.getBotAttackable());
    this.findTargets(this.getBotAttackable(), super.getPlayerAttackable());
  }

  private void attackTargets(final List<Attackable> selfAttackables) {
    selfAttackables.forEach(attackable -> {
      attackable.attackCurrentTarget();
      if (attackable.getCurrentTarget().isPresent()) {
        final var currentTarget = attackable.getCurrentTarget().get();
        if (currentTarget.isDead()) {
          if (isUserTheOwner(currentTarget)) {
            super.removeUserAttackableFromArena(currentTarget);
          } else {
            this.removeBotAttackableFromArena(currentTarget);
          }
          attackable.resetCurrentTarget();
        }
      }
    });
  }

  private void removeBotAttackableFromArena(final Attackable target) {
    if (super.isTower(target)) {
      this.destroyBotTower((Tower) target);
    } else {
      this.removeBotCardFromMap((Card) target);
    }
  }

  @Override
  public void handleAttackTargets() {
    this.attackTargets(super.getPlayerAttackable());
    this.attackTargets(this.getBotAttackable());
  }
}

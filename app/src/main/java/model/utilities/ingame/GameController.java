package model.utilities.ingame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;

import model.actors.Attackable;
import model.actors.cards.Card;
import model.actors.towers.KingTower;
import model.actors.towers.QueenTower;
import model.actors.towers.Tower;
import model.actors.users.User;
import model.utilities.AnimationUtilities;
import model.utilities.ElixirController;

/**
 * Defines the logic to be used inside the game.
 */
public abstract class GameController {

  /**
   * the number of cards that can be chosen in every moment.
   */
  protected static final int CHOOSABLE_CARDS = 4;

  private final List<Card> playerCards;
  private final List<Card> playerDeployedCards;
  private final List<Card> playerChoosableCards;
  private final List<Tower> playerActiveTowers;
  private final ElixirController elixirController;

  /**
   * 
   * @param playerCards
   *              the player deck.
   * @param user
   *              the user who is playing.
   * @param stage 
   *              the stage the gameController has to control.
   */
  public GameController(final List<Card> playerCards, final User user, final Stage stage) {
    this.playerCards = playerCards.stream().collect(Collectors.toList());
    this.playerDeployedCards = new ArrayList<>();
    this.playerChoosableCards = new ArrayList<>();
    IntStream.range(0, CHOOSABLE_CARDS).forEach(i -> this.playerChoosableCards.add(this.playerCards.remove(0)));
    this.playerActiveTowers = this.getPlayerTowers(user, stage);
    this.elixirController = new ElixirController();
  }

  /* logica per la posizione delle torri nella mappa mancante */
  private List<Tower> getPlayerTowers(final User user, final Stage stage) {
    final List<Tower> towers = new ArrayList<>();
    towers.add(QueenTower.create(user, stage, new Vector2(205, 312)));
    towers.add(QueenTower.create(user, stage, new Vector2(415, 312)));
    towers.add(KingTower.create(user, stage, new Vector2(300, 255)));
    towers.forEach(t -> {
      if (t.getClass() == QueenTower.class) {
        t.setAnimation(AnimationUtilities.loadTexture("towers/self/queen_tower.png"));
      } else {
        t.setAnimation(AnimationUtilities.loadTexture("towers/self/king_tower.png"));
      }
    });
    return towers;
  }


  /**
   * 
   * @return a list of user currently deployed cards.
   */
  public List<Card> getPlayerDeployedCards() {
    return Collections.unmodifiableList(this.playerDeployedCards);
  }

  /**
   * 
   * @return a list of user currently choosable cards.
   */
  public List<Card> getPlayerChoosableCards() {
    return Collections.unmodifiableList(this.playerChoosableCards);
  }

  /**
   * Deploys a card of the player.
   * @param card
   *           the card to be deployed.
   */
  public void deployPlayerCard(final Card card) {
    if (this.playerChoosableCards.contains(card)) {
      this.playerChoosableCards.remove(card);
      this.elixirController.decrementElixir(card.getCost());
      this.playerDeployedCards.add(card);
      this.playerCards.add(card);
    }
  }

  /**
   * Removes a card from the map.
   * @param card
   *           the card to be removed.
   */
  public void removePlayerCardFromMap(final Card card) {
    if (this.playerDeployedCards.contains(card)) {
      this.playerDeployedCards.remove(card);
    }
  }

  /**
   * 
   * @return the currently active towers of the user.
   */
  public List<Tower> getPlayerActiveTowers() {
    return Collections.unmodifiableList(this.playerActiveTowers);
  }

  /**
   * If not already, destroys a user tower.
   * 
   * @param tower
   *            the tower to be destroyed.
   */
  public void destroyUserTower(final Tower tower) {
    if (this.playerActiveTowers.contains(tower)) {
      this.playerActiveTowers.remove(tower);
    }
  }

  /**
   * 
   * @return the current elixir left to the player.
   */
  public int getPlayerElixirLeft() {
    return this.elixirController.getElixirCount();
  }

  /**
   * 
   * @return a list of attackable elements of the player.
   */
  public List<Attackable> getPlayerAttackable() {
    /* ricorda di sostituire con playerDeployedCards */
    return Stream.concat(this.playerChoosableCards.stream().map(c -> (Attackable) c), this.playerActiveTowers.stream().map(t -> (Attackable) t)).collect(Collectors.toList());
  }

}

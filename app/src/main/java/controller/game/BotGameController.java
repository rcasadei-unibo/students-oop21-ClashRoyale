package controller.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;

import controller.ElixirController;

import model.GlobalData;
import model.entities.Attackable;
import model.entities.cards.Card;
import model.entities.towers.Tower;
import model.deck.PlayersDeck;
import model.utilities.AnimationUtilities;
import model.utilities.ingame.BotGameModel;
import view.actors.cards.CardActor;
import view.actors.towers.TowerActor;

/**
 * 
 * Game controller implementation Simple Bot mode.
 */
public class BotGameController extends GameController {

  private final ElixirController botElixir;
  private Map<CardActor, Card> botCardsMap;
  private Map<TowerActor, Tower> botTowersMap;
  private final BotAiController botController;
  private final Random rand = new Random();
  private final JFrame frame;

  /**
   * Constructor.
   */
  public BotGameController() {
    super(new BotGameModel(PlayersDeck.getInstance().cardList(), GlobalData.BOT_DECK, GlobalData.USER, GlobalData.BOT));
    this.botElixir = new ElixirController();
    this.botCardsMap = new HashMap<>();
    this.botTowersMap = new HashMap<>();
    this.frame = new JFrame();
    this.botController = new BotAiController(this.randomCard());
  }

  @Override
  protected boolean checkEnemyLose() {
    return ((BotGameModel) super.getModel()).getBotActiveTowers().size() == 0;
  }

  @Override
  protected int getDestoryedTowers() {
    return 3 - ((BotGameModel) super.getModel()).getBotActiveTowers().size();
  }

  @Override
  protected void onUpdate() {
    this.botElixir.setRunFalse();
  }

  /**
   * @return the current elixir owned by the bot.
   */
  public int getBotCurrentElixir() {
    return this.botElixir.getElixirCount();
  }

  /**
   * 
   * @return a list of the bot attackable entities.
   */
  public List<Attackable> getBotAttackables() {
    return ((BotGameModel) super.getModel()).getBotAttackable();
  }

  @Override
  protected void onLoadActors(final Stage stage) {
    this.botCardsMap = super.loadCardActorsFrom(((BotGameModel) super.getModel()).getBotDeck(), stage, "ENEMY_MOVING");
  }

  @Override
  protected void onLoadTowers(final Stage stage) {
    this.botTowersMap = super.loadTowerActorsFrom(((BotGameModel) super.getModel()).getBotActiveTowers(), stage, "ENEMY");
  }

  @Override
  protected void onUpdateActorAnimations() {
    super.updateCardAnimations(this.botCardsMap, "ENEMY_MOVING", "ENEMY_FIGHTING");
    super.updateTowerAnimations(this.botTowersMap, "ENEMY");
  }

  private void updateAttackablePosition(final Attackable attackable, final List<Attackable> enemies) {
    attackable.setPosition(this.getGameMap().getNextPosition(attackable, enemies));
  }

  private void updateActorPositions(final Map<CardActor, Card> cardActors, final List<Attackable> enemyAttackables) {
    final var actor = new ArrayList<CardActor>();
    cardActors.entrySet().stream().forEach(e -> {
      if (!e.getKey().isDraggable() && e.getKey().getCenter().equals(e.getValue().getPosition())) {
        this.updateAttackablePosition((Attackable) e.getValue(), enemyAttackables);
        e.getKey().setRotation(e.getValue().getPosition());
        e.getKey().moveTo(e.getValue().getPosition());
      }
      if (((Attackable) e.getValue()).getCurrentTarget().isPresent()) {
        e.getKey().setRotation(((Attackable) e.getValue()).getCurrentTarget().get().getPosition());
      }
      if (((Attackable) e.getValue()).isDead()) {
        actor.add(e.getKey());
      }
    });
    if (!actor.isEmpty()) {
      super.updateCardsMap(actor);
    }
  }

  private void placeBotActor() {
    this.botController.setBotCardsMap(this.botCardsMap);
    this.botController.setElixir(this.getBotCurrentElixir());
    final Vector2 randomPosition = new Vector2(this.randomPosition(150, 550), this.randomPosition(500, 700));
    if (this.checkposition(randomPosition, this.botController.getRandomCard())) {
      this.botController.setRandomPosition(randomPosition);
    }
    final Card cardDeployed = this.botController.getCardDeployed();
    if (cardDeployed != null) {
      this.deployBotCard(cardDeployed);
      this.botController.setRandomCard(this.randomCard());
    }
    final CardActor cardActorDeployed = this.botController.getCardActorDeployed();
    if (cardActorDeployed != null) {
    final var nextCard = ((BotGameModel) super.getModel()).getBotNextQueuedCard(cardActorDeployed.getOrigin());
    if (nextCard.isPresent()) {
      this.botCardsMap.put(
          new CardActor(cardActorDeployed.getOrigin().x, cardActorDeployed.getOrigin().y, cardActorDeployed.getStage(), AnimationUtilities.loadAnimationFromFiles(nextCard.get().getAnimationFiles().get("ENEMY_MOVING"), ANIMATIONS_FRAME_DURATION, true)),
          nextCard.get());
    }
    }
  }

  private void deployBotCard(final Card card) {
    ((BotGameModel) super.getModel()).deployBotCard(card);
    this.botElixir.decrementElixir(card.getCost());
  }

  private int randomPosition(final int min, final int max) {
    return rand.nextInt((max - min) + 1) + min;
  }
  private Card randomCard() {
    return ((BotGameModel) super.getModel()).getBotChoosableCards().get(rand.nextInt(((BotGameModel) super.getModel()).getBotChoosableCards().size()));
  }

  @Override
  protected void onUpdateActors() {
    this.placeBotActor();
    this.updateActorPositions(super.getPlayerActorsMap(), this.getBotAttackables());
    this.updateActorPositions(this.botCardsMap, super.getUserAttackables());
  }

  /**
   * 
   * @return the current towers destroyed by the user.
   */
  public long getPlayerScore() {
    return botTowersMap.entrySet().stream().filter(e -> e.getValue().isDead()).count();
  }

  @Override
  public boolean checkForwinner() {
    if (getPlayerScore() == 3 || super.getBotScore() == 3) {
      return true;
      }
    return false;
  }

  @Override
  public void recordResult() {
    if (getPlayerScore() == super.getBotScore()) {
      JOptionPane.showMessageDialog(frame, "Pareggio");
    } else if (getPlayerScore() > super.getBotScore()) {
      JOptionPane.showMessageDialog(frame, "Hai Vinto");
      GlobalData.USER.awardXp((int) getPlayerScore());
    } else {
    JOptionPane.showMessageDialog(frame, "Hai Perso");
    GlobalData.USER.pointReduction();
    }
  }
}

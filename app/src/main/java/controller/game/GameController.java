package controller.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import controller.Controller;
import controller.CountDownController;
import controller.ElixirController;
import controller.audio.AudioGameController;
import controller.menu.MenuController;

import launcher.ClashRoyale;
import model.GameModel;
import model.entities.Attackable;
import model.entities.cards.Card;
import model.entities.towers.Tower;
import model.entities.users.Bot;
import model.entities.users.User;
import model.map.GameMap;
import model.map.MapUnit;
import utilities.AnimationUtilities;
import view.actors.cards.CardActor;
import view.actors.towers.TowerActor;
import view.screens.game.GameScreen;

/**
 * Abstract Controller for the game screen.
 */
public abstract class GameController extends Controller {

  /**
   * How much each frame has to last during the animation.
   */
  protected static final float ANIMATIONS_FRAME_DURATION = (float) 0.017_24 * 10;
  private static final int MIDDLE = 500;

  private final CountDownController timer;
  private final ElixirController playerElixir;
  private final GameMap gameMap;
  private Map<CardActor, Card> playerCardsMap;
  private Map<TowerActor, Tower> playerTowersMap;

  /**
   * Constructor.
   * 
   * @param model the logic followed by this controller.
   */
  public GameController(final GameModel model) {
    super(new AudioGameController());
    super.playMusic();
    this.timer = new CountDownController();
    this.playerElixir = new ElixirController();
    this.gameMap = new GameMap();
    this.playerCardsMap = new HashMap<>();
    this.playerTowersMap = new HashMap<>();
    super.registerModel(model);
  }

  /**
   * 
   * @return the current game map.
   */
  protected GameMap getGameMap() {
    return this.gameMap;
  }

  @Override
  public void update(final float dt) {
    if (this.timer.getTime() == 0 || this.checkUserLose() || this.checkEnemyLose() || this.checkForwinner()) {
      this.playerElixir.setRunFalse();
      this.updateUserStatistics();
      this.onUpdate();
      this.timer.setRunFalse();
      super.stopMusic();
      new MenuController().setCurrentActiveScreen();
    }
    this.updateActors();
    this.updateActorAnimations();
  }

  /**
   * Updates the game statistics for the user, based on the game outcome.
   */
  protected abstract void updateUserStatistics();

  private boolean checkUserLose() {
    return ((GameModel) super.getModel()).getPlayerActiveTowers().size() == 0;
  }

  /**
   * 
   * @return if the enemy, whether is a bot or a real player, lost the match.
   */
  protected abstract boolean checkEnemyLose();

  /**
   * 
   * @return the number of enemy, whether is a bot or a real player, destroyed towers.
   */
  protected abstract int getEnemyDestoryedTowers();

  /**
   * Called from subclasses to extend functionalities when the match is over.
   */
  protected abstract void onUpdate();

  /**
   * 
   * @return the remaining seconds before game ends.
   */
  public int getLeftTime() {
    return this.timer.getTime();
  }

  /**
   * 
   * @return the current elixir owned by the user.
   */
  public int getPlayerCurrentElixir() {
    return this.playerElixir.getElixirCount();
  }

  /**
   * 
   * @return a list of the user attackable entities.
   */
  protected List<Attackable> getUserAttackables() {
    return ((GameModel) super.getModel()).getPlayerAttackable();
  }

  /**
   * Load new card actors in the stage passed as argument, picking them
   * informations from the list passed as argument.
   * 
   * @param list
   *             the list of Cards used to create new actors.
   * @param stage
   *             where actors have to be placed.
   * @param animationName 
   *             the animation of the actors.
   * @return a {@link Collection} that map each CardActor to its model entity.
   */
  protected final Map<CardActor, Card> loadCardActorsFrom(final List<Card> list, final Stage stage, final String animationName) {
    final var actors = new HashMap<CardActor, Card>();
    list.forEach(c -> {
      final var actor = new CardActor(c.getPosition().x, c.getPosition().y, stage, AnimationUtilities.loadAnimationFromFiles(c.getAnimationFiles().get(animationName), ANIMATIONS_FRAME_DURATION, true));
      actors.put(actor, c);
    });
    return actors;
  }

  /**
   * Load card actors in a stage of the screen driven by this controller.
   * 
   * @param stage the stage where actors have to be placed.
   */
  public final void loadActors(final Stage stage) {
    this.playerCardsMap = this.loadCardActorsFrom(((GameModel) super.getModel()).getPlayerChoosableCards(), stage, "AS_CARD");
    this.loadEnemyActors(stage);
  }

  /**
   * Template method used to allow subclasses to load their actors.
   * 
   * @param stage where actors have to be placed.
   */
  protected abstract void loadEnemyActors(Stage stage);

  /**
   * Load new tower actors in the stage passed as argument, picking them
   * informations from the list passed as argument.
   * 
   * @param list
   *              the list of Towers used to create new actors.
   * @param stage
   *              where actors have to be placed.
   * @param animationName 
   *              the animation of the actors.
   * @return a {@link Collection} that map each TowerActor to its model entity.
   */
  protected final Map<TowerActor, Tower> loadTowerActorsFrom(final List<Tower> list, final Stage stage, final String animationName) {
    final var towers = new HashMap<TowerActor, Tower>();
    list.forEach(t -> {
      final var actor = new TowerActor(t.getPosition().x, t.getPosition().y, stage, AnimationUtilities.loadAnimationFromFiles(t.getAnimationFiles().get(animationName), ANIMATIONS_FRAME_DURATION, true));
      actor.setPosition(actor.getPosition().x, actor.getPosition().y);
      towers.put(actor, t);
    });
    return towers;
  }

  /**
   * Load tower actors in the main stage of the screen driven by this controller.
   * 
   * @param stage the stage where towers have to be placed.
   */
  public final void loadTowers(final Stage stage) {
    this.playerTowersMap = this.loadTowerActorsFrom(((GameModel) super.getModel()).getPlayerActiveTowers(), stage, "SELF");
    this.loadEnemyTowers(stage);
  }

  /**
   * Template method used to allow subclasses to load their actors.
   * 
   * @param stage where towers have to be laced.
   */
  protected abstract void loadEnemyTowers(Stage stage);

  /**
   * Update a user (whether is a bot or real player) card actor animations based
   * on their status.
   * 
   * @param playerCardsMap 
 *                         a map that associate each card actor to its own card.
   * @param moving
   *                       the name of the files used for moving animations.
   * @param fighting
   *                       the name of the files used for fighting animations.
   */
  protected void updateCardAnimations(final Map<CardActor, Card> playerCardsMap, final String moving, final String fighting) {
    playerCardsMap.entrySet().stream().forEach(e -> {
      if (this.getGameMap().containsPosition(e.getKey().getCenter()) && !e.getKey().isDraggable()) {
        e.getKey().setAnimation(AnimationUtilities.loadAnimationFromFiles(e.getValue().getAnimationFiles().get(((Attackable) e.getValue()).getCurrentTarget().isPresent() ? fighting : moving), ANIMATIONS_FRAME_DURATION, true));
      }
    });

  }

  /**
   * Update a user (whether is a bot or real player) tower actor animations based
   * on their status.
   * 
   * @param playerTowersMap 
   *                        a map that associate each tower actor to its own tower.
   * @param standing
   *                        the name of files used for animating towers.
   */
  protected void updateTowerAnimations(final Map<TowerActor, Tower> playerTowersMap, final String standing) {
    playerTowersMap.entrySet().stream().forEach(e -> {
      if (((Attackable) e.getValue()).isDead()) {
        e.getKey().setAngle(0);
        e.getKey().setAnimation(AnimationUtilities.loadAnimationFromFiles(e.getValue().getAnimationFiles().get("DESTROYED"), ANIMATIONS_FRAME_DURATION, true));
      } else if (((Attackable) e.getValue()).getCurrentTarget().isPresent()) {
        e.getKey().setRotation(e.getValue().getCurrentTarget().get().getPosition());
        e.getKey().setAnimation(AnimationUtilities.loadAnimationFromFiles(e.getValue().getAnimationFiles().get("FIGHTING"), ANIMATIONS_FRAME_DURATION, true));
      } else {
        e.getKey().setAngle(0);
        e.getKey().setAnimation(AnimationUtilities.loadAnimationFromFiles(e.getValue().getAnimationFiles().get(standing), ANIMATIONS_FRAME_DURATION, true));
      }
    });
  }

  private void updateActorAnimations() {
    this.updateCardAnimations(this.playerCardsMap, "SELF_MOVING", "SELF_FIGHTING");
    this.updateTowerAnimations(this.playerTowersMap, "SELF");
    this.updateEnemyActorAnimations();
  }

  /**
   * Template method used to allow subclasses update their actor animations.
   */
  protected abstract void updateEnemyActorAnimations();

  private void updateActors() {
    ((GameModel) super.getModel()).findAttackableTargets();
    ((GameModel) super.getModel()).handleAttackTargets();
    this.placePlayerActors();
    this.updateEnemyActors();
  }

  /**
   * Template method implemented by subclasses to update actor positions.
   */
  protected abstract void updateEnemyActors();

  private void placePlayerActors() {
    final var card = new ArrayList<Card>();
    this.getPlayerActorsMap().entrySet().stream().forEach(e -> {
      if (e.getKey().isDraggable() && !Gdx.input.isTouched()) {
        if (this.checkposition(e.getKey().getCenter(), e.getValue()) && e.getValue().getCost() <= this.getPlayerCurrentElixir()) {
          card.add(e.getValue());
          this.deployPlayerCard(e.getValue());
          e.getKey().setDraggable(false);
          e.getValue().setPosition(e.getKey().getCenter());
        } else {
          e.getKey().setPosition(e.getKey().getOrigin().x, e.getKey().getOrigin().y);
        }
      }
    });
    if (!card.isEmpty()) {
      this.deployPlayerActor(card);
    }
  }

  /**
   * Check if a card can be placed on a certain position.
   * 
   * @param v 
   *            a {@link Vector2} describing the position in which the card wants to be placed.
   * @param c card to place
   * @return boolean
   */ 
  protected boolean checkposition(final Vector2 v, final Card c) {
    if (this.getGameMap().containsPosition(v) && this.getGameMap().getMapUnitFromPosition(v).getType().equals(MapUnit.Type.TERRAIN)) {
      if (c.getOwner() instanceof User && v.y < MIDDLE) {
        return true;
      } else if (c.getOwner() instanceof Bot && v.y > MIDDLE) {
        return true;
      }
    }
    return false;
  }

  /**
   * Perform an update of both model and elixir controller after a card has been
   * deployed.
   * 
   * @param card
   */
  protected void deployPlayerCard(final Card card) {
    ((GameModel) super.getModel()).deployPlayerCard(card);
    this.playerElixir.decrementElixir(card.getCost());
  }

  /**
   * add bot troops in map.
   * 
   * @param elements list of card.
   */
  protected void deployPlayerActor(final List<Card> elements) {
    elements.stream().forEach(card -> {
      CardActor c = null;
      for (final Entry<CardActor, Card> entry : this.playerCardsMap.entrySet()) {
        if (entry.getValue().equals(card)) {
          c = entry.getKey();
          this.deployPlayerCard(card);
        }
      }
      if (c != null) {
      final var nextCard = ((GameModel) super.getModel()).getPlayerNextQueuedCard(c.getOrigin());
      if (nextCard.isPresent()) {
        this.playerCardsMap.put(
            new CardActor(c.getOrigin().x, c.getOrigin().y, c.getStage(), AnimationUtilities.loadAnimationFromFiles(nextCard.get().getAnimationFiles().get("AS_CARD"), ANIMATIONS_FRAME_DURATION, true)),
            nextCard.get());

      }
      }
    });
  }

  /**
   * @return a copy of player card actors.
   */
  protected Map<CardActor, Card> getPlayerActorsMap() {
    return this.playerCardsMap;
  }

  /**
   * @return a copy of player tower actors.
   */
  protected Map<TowerActor, Tower> getPlayerTowersMap() {
    return this.playerTowersMap;
  }

  /**
   * 
   * @return the number of current user destroyed towers.
   */
  public int getPlayerDestroyedTowers() {
    return (int) this.playerTowersMap.entrySet()
        .stream()
        .filter(s -> s.getValue().isDead())
        .count();
  }

  /**
   * 
   * @return the number of current enemy destroyed towers.
   */
  public abstract int getEnemyDestroyedTowers();

  /**
   * 
   * update Cards Map.
   * 
   * @param elements list of card.
   */
  protected void updateCardsMap(final List<CardActor> elements) {
    elements.stream()
      .peek(Actor::remove)
      .forEach(c -> this.playerCardsMap.remove(c));
  }

/**
 * Check for a winner.
 * 
 * @return whether a winner exists or not at the moment this method is called.
 */
  public abstract boolean checkForwinner();

  @Override
  public void setCurrentActiveScreen() {
    ClashRoyale.setActiveScreen(new GameScreen(this));
  }

}


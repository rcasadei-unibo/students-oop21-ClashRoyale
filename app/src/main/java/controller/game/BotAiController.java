package controller.game;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.math.Vector2;

import model.entities.cards.Card;

import view.actors.cards.CardActor;
/**
 * Game will least 90 seconds.
 */
public class BotAiController {
  /**
   * Game will least 90 seconds.
   */
  public static final int DEFAULT_TIME = 90;
  private final Timer timer;
  private boolean run;
  private int time;
  private CardActor cardActorDeployed;
  private Card cardDeployed;
  private Card randomCard;
  private Vector2 randomPosition;
  private int elixir;
  private Map<CardActor, Card> botCardsMap;

  /**
   * build an bot AI controller.
   * @param card
   */
  public BotAiController(final Card card) {
    this.time = DEFAULT_TIME;
    this.run = true;
    this.timer = new Timer();
    this.randomCard = card;
    final var task = new TimerTask() {
      @Override
      public void run() {
        if (time > 0 && run) {
          cardActorDeployed = null;
          cardDeployed = null;
          for (final Entry<CardActor, Card> e : botCardsMap.entrySet()) {
            if (e.getKey().isDraggable() && e.getValue().getCost() <= elixir && e.getValue().equals(randomCard)) {
              cardActorDeployed = e.getKey();
              cardDeployed = e.getValue();
              e.getKey().setPosition(randomPosition.x, randomPosition.y);
              e.getValue().setPosition(e.getKey().getCenter());
              e.getKey().setDraggable(false);
            }
          }
          time--;
        } else {
          timer.cancel();
          timer.purge();
       }
     }
     };
    this.timer.schedule(task, 1000, 1000);
  }
   /**
    * update elixir of the bot.
    * @param elixir
    */
  public void setElixir(final int elixir) {
    this.elixir = elixir;
  }
  /**
   * @return CardActor deployed. 
   */
  public CardActor getCardActorDeployed() {
    return cardActorDeployed;
  }

  /**
   * @return Card deployed.
   */
  public Card getCardDeployed() {
    return cardDeployed;
  }
  /**
   * @return random card.
   */
  public Card getRandomCard() {
    return randomCard;
  }
  /**
   * set randomCard to deploy.
   * @param randomCard
   */
  public void setRandomCard(final Card randomCard) {
    this.randomCard = randomCard;
  }
  /**
   * set random Position for the card to deploy.
   * @param randomPosition
   */
  public void setRandomPosition(final Vector2 randomPosition) {
    this.randomPosition = randomPosition;
  }
  /**
   * update botCardsMap.
   * @param botCardsMap
   */
  public void setBotCardsMap(final Map<CardActor, Card> botCardsMap) {
    this.botCardsMap = botCardsMap;
  }

  /**
    * set return to false.
    */
   public void setRunFalse() {
     this.run = false;
   }
   /**
    * @return the remaining seconds before game ends.
    */
   public int getTime() {
     return this.time;
   }

   /**
    * Restart the timer.
    */
   public void setTime() {
     this.time = DEFAULT_TIME;
   }
}

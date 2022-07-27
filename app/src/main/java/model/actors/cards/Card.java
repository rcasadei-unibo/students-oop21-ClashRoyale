package model.actors.cards;

import java.util.Objects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import model.actors.DragAndDropActor;
import model.actors.users.User;

/**
 * Defines a game card.
 */
public abstract class Card extends DragAndDropActor {

  private final int cost;
  private final User owner;

  /** 
   * @param stage
   *          {@inheritDoc}
   * @param cost
   *          elixir cost of the card.
   * @param position
   *          x,y coordinates where the card is placed.
   * @param owner
   *          the user who deployed the card.
   */
  protected Card(final Stage stage, final int cost, final Vector2 position, final User owner) {
    super(position.x, position.y, stage);
    this.cost = cost;
    this.owner = owner;
  }

  /**
   * @return the owner of the card.
   */
  public User getOwner() {
    return this.owner;
  }

  /**
   * @return the cost of the card.
   */
  public int getCost() {
    return this.cost;
  }

  @Override
  public int hashCode() {
    return Objects.hash(cost, owner);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false; 
    }
    final Card other = (Card) obj;
    return cost == other.cost && Objects.equals(owner, other.owner);
  }
}

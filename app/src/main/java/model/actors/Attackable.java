package model.actors;

import com.badlogic.gdx.math.Vector2;

/**
 * Defines entities which are allowed to be attacked from others.
 */
public interface Attackable {

  /** 
   * @return the current position of the entity that implements this interface.
   */
  Vector2 getPosition();

  /**
   * Updates the current position of the entity.
   * @param newPos
   *              the new position.
   */
  void setPosition(Vector2 newPos);

  /**
   * Reduces the health of the entity.
   * @param damage
   *              the amount of life to be taken.
   */
  void reduceHPBy(double damage);

  /**
   * @return whether the entity is dead or not.
   */
  boolean isDead();

  /**
   * @return the type of the entity (i.e. air troop, ground troop).
   */
  TargetType getSelfType();

}

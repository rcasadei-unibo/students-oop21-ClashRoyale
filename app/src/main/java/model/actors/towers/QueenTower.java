package model.actors.towers;

import com.badlogic.gdx.math.Vector2;

import model.actors.Speeds;
import model.actors.users.User;

/**
 * The corner towers.
 */
public final class QueenTower extends Tower {

  private static final double RANGE = 100;
  private static final Speeds HIT_SPEED = Speeds.MEDIUM;

  private QueenTower(final Vector2 position, final User owner, final double damage, final double hp) {
    super(position, owner, QueenTower.RANGE, true, damage, hp, QueenTower.HIT_SPEED);
  }

  /**
   * Builds a new QueenTower based on the owner level.
   * 
   * @param owner
   *            {@inheritDoc}.
   * @param position
   *            {@inheritDoc}.
   * @return a new QueenTower object.
   */
  public static QueenTower create(final User owner, final Vector2 position) {
    switch (owner.getCurrentLevel()) {
      case LVL1: return new QueenTower(position, owner, 50, 1400);
      case LVL2: return new QueenTower(position, owner, 54, 1512);
      case LVL3: return new QueenTower(position, owner, 58, 1624);
      case LVL4: return new QueenTower(position, owner, 62, 1750);
      case LVL5: return new QueenTower(position, owner, 67, 1890);
      default: return new QueenTower(position, owner, 50, 1400);
    }
  }
}

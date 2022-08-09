package model.actors.cards.buildings;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;

import model.actors.users.User;
import model.actors.Attackable;

/**
 * Inferno Tower building.
 */
public final class InfernoTower extends Building {

  private static final int ELIXIR_COST = 3;
  private static final int RANGE = 3;
  private static final double HIT_SPEED = 0.4;

  private final List<Attackable> targets;

  private InfernoTower(final User owner, final Vector2 position, final double maxHP, final double damage) {
    super(InfernoTower.ELIXIR_COST, position, owner, maxHP, damage, InfernoTower.HIT_SPEED, InfernoTower.RANGE);
    this.targets = Collections.emptyList();
  }

  /**
   * Reduce the HP of targets.
   */
  public void attack() {
      this.targets.forEach(target -> target.reduceHPBy(this.getDamage()));
  }

  /**
   * Add target.
   *
   * @param target the target
   */
  public void addTarget(final Attackable target) {
      if (!this.targets.contains(target)) {
          this.targets.add(target);
      }
  }

  /**
   * Create an InfernoTowre card based on user level.
   * 
   * @param user
   *          who wants to deploy the Inferno Tower.
   * @param position
   *          x,y coordinates.
   * @return the InfernoTower itself.
   */
  public static Building create(final User user, final Vector2 position) {
    switch (user.getCurrentLevel()) {
      case LVL1: return new InfernoTower(user, position, 800, 210);
      case LVL2: return new InfernoTower(user, position, 880, 231);
      case LVL3: return new InfernoTower(user, position, 968, 254);
      case LVL4: return new InfernoTower(user, position, 1064, 279);
      case LVL5: return new InfernoTower(user, position, 1168, 307);
      default:   return new InfernoTower(user, position, 800, 210);
      }
  }

  @Override
  public Map<String, List<String>> getAnimationFiles() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setCenter(Vector2 newPos) {
    // TODO Auto-generated method stub
    
  }
}

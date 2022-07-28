package model.utilities;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

/**
 * Disegna i rettangolini.
 */
public final class RectDrawer {

  private static ShapeRenderer debugShapeRenderer = new ShapeRenderer();

  private RectDrawer() {
  }

  /**
   * Disegna i rettangoli.
   * @param boundingBoxes
   *                  i rettangoli.
   * @param color
   *                  il colore.
   */
  public static void showDebugBoundingBoxes(final List<Rectangle> boundingBoxes, final Color color) {
      debugShapeRenderer.begin(ShapeType.Line); // make sure to end the spritebatch before you call this line
      debugShapeRenderer.setColor(color);
      for (final Rectangle rect : boundingBoxes) {
          debugShapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
      }
      debugShapeRenderer.end();
  }
}

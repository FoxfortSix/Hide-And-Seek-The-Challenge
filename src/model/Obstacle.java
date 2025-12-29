package model;

/**
 * Represents a static obstacle within the game world.
 * <p>
 * The {@code Obstacle} class models non-movable environmental objects
 * such as rocks or barriers that block movement and projectiles.
 * It extends {@link GameObject} and inherits spatial properties
 * including position and collision dimensions.
 * </p>
 *
 * <p>
 * This class provides multiple constructors to support obstacles
 * of varying sizes, enabling visual and gameplay variation
 * without requiring additional subclasses.
 * </p>
 *
 * @author Mochammad Azka Basria
 */
public class Obstacle extends GameObject {

    /**
     * Constructs a standard-sized obstacle.
     * <p>
     * This constructor initializes the obstacle with a default
     * square dimension, typically used for common environmental
     * objects.
     * </p>
     *
     * @param x the horizontal position in world coordinates
     * @param y the vertical position in world coordinates
     */
    public Obstacle(double x, double y) {
        super(x, y, 50, 50);
    }

    /**
     * Constructs a custom-sized obstacle.
     * <p>
     * This constructor allows the creation of obstacles with
     * variable dimensions to introduce level design diversity
     * and different collision behaviors.
     * </p>
     *
     * @param x      the horizontal position in world coordinates
     * @param y      the vertical position in world coordinates
     * @param width  the width of the obstacle
     * @param height the height of the obstacle
     */
    public Obstacle(double x, double y, int width, int height) {
        super(x, y, width, height);
    }
}

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import processing.core.PImage;

public class Fairy extends Actionable implements Moveable{

    public static final String FAIRY_KEY = "fairy";

    public Fairy(String id, Point position, List<PImage> images, double animationPeriod,
            double behaviorPeriod) {
        super(id, position, images, 0, animationPeriod, behaviorPeriod);
    }

    public boolean move(World world, Entity target, EventScheduler scheduler) {
        if (getPosition().adjacentTo(target.getPosition())) {
            world.removeEntity(scheduler, target);
            return true;
        } else {
            Point nextPos = nextPosition(world, target.getPosition());
            if (!getPosition().equals(nextPos)) {
                world.moveEntity(scheduler, this, nextPos);
            }
            return false;
        }
    }

    /** Determines a Fairy's next position when moving. */
    public Point nextPosition(World world, Point destination) {
        // Differences between the destination and current position along each axis
        int deltaX = destination.x - this.getPosition().x;
        int deltaY = destination.y - this.getPosition().y;

        // Positions one step toward the destination along each axis
        Point horizontalPosition = new Point(this.getPosition().x + Integer.signum(deltaX), this.getPosition().y);
        Point verticalPosition = new Point(this.getPosition().x, this.getPosition().y + Integer.signum(deltaY));

        // Assumes all destinations are within bounds of the world
        // If this is not the case, also check 'World.withinBounds()'
        boolean horizontalOccupied = world.isOccupied(horizontalPosition);
        boolean verticalOccupied = world.isOccupied(verticalPosition);

        // Move along the farther direction, preferring horizontal
        if (Math.abs(deltaX) >= Math.abs(deltaY)) {
            if (!horizontalOccupied) {
                return horizontalPosition;
            } else if (!verticalOccupied) {
                return verticalPosition;
            }
        } else {
            if (!verticalOccupied) {
                return verticalPosition;
            } else if (!horizontalOccupied) {
                return horizontalPosition;
            }
        }

        return getPosition();
    }

    public void executeActivity(World world, ImageLibrary imageLibrary, EventScheduler scheduler) {
        Optional<Entity> fairyTarget = world.findNearest(getPosition(), new ArrayList<>(List.of(Stump.class)));

        if (fairyTarget.isPresent()) {
            Point tgtPos = fairyTarget.get().getPosition();

            if (move(world, fairyTarget.get(), scheduler)) {
                Sapling sapling = new Sapling(Sapling.SAPLING_KEY + "_" + fairyTarget.get().getId(), tgtPos, imageLibrary.get(Sapling.SAPLING_KEY));

                world.addEntity(sapling);
                sapling.scheduleActions(scheduler, world, imageLibrary);
            }
        }

        scheduleBehavior(scheduler, world, imageLibrary);
    }

    

    public void updateImage() {
        this.setImageIndex(this.getImageIndex() + 1);
    }

}

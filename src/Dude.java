import java.util.List;
import java.util.Optional;

import processing.core.PImage;

public class Dude extends Resourceable implements Moveable{

    public static final String DUDE_KEY = "dude";

    public Dude(String id, Point position, List<PImage> images, double animationPeriod,
            double behaviorPeriod, int resourceCount, int resourceLimit) {
        super(id, position, images, 0, animationPeriod, behaviorPeriod, resourceLimit, resourceCount, 0);
    }


    public boolean transformDude(World world, EventScheduler scheduler, ImageLibrary imageLibrary) {
        if (this.getResourceCount() < this.getResourceLimit()) {
            this.setResourceCount(this.getResourceCount() + 1);
            if (this.getResourceCount() == this.getResourceLimit()) {
                Dude dude = new Dude(this.getId(), this.getPosition(), imageLibrary.get(DUDE_KEY + "_carry"), this.getAnimationPeriod(), this.getBehaviorPeriod(), this.getResourceCount(), this.getResourceLimit());

                world.removeEntity(scheduler, this);

                world.addEntity(dude);
                dude.scheduleActions(scheduler, world, imageLibrary);

                return true;
            }
        } else {
            Dude dude = new Dude(this.getId(), this.getPosition(), imageLibrary.get(DUDE_KEY), this.getAnimationPeriod(), this.getBehaviorPeriod(), 0, this.getResourceLimit());

            world.removeEntity(scheduler, this);

            world.addEntity(dude);
            dude.scheduleActions(scheduler, world, imageLibrary);

            return true;
        }

        return false;
    }


    public boolean move(World world, Entity target, EventScheduler scheduler) {
        if (this.getPosition().adjacentTo(target.getPosition())) {
            if (target instanceof Tree || target instanceof Sapling) {
                ((Resourceable)target).setHealth(((Resourceable)target).getHealth() -1);
            }
            return true;
        } else {
            Point nextPos = nextPosition(world, target.getPosition());

            if (!target.getPosition().equals(nextPos)) {
                world.moveEntity(scheduler, this, nextPos);
            }

            return false;
        }
    }

    public Point nextPosition(World world, Point destination) {
        // Differences between the destination and current position along each axis
        int deltaX = destination.x - this.getPosition().x;
        int deltaY = destination.y - this.getPosition().y;

        // Positions one step toward the destination along each axis
        Point horizontalPosition = new Point(this.getPosition().x + Integer.signum(deltaX), this.getPosition().y);
        Point verticalPosition = new Point(this.getPosition().x, this.getPosition().y + Integer.signum(deltaY));

        // Assumes all destinations are within bounds of the world
        // If this is not the case, also check 'World.inBounds()'
        boolean horizontalOccupied = world.isOccupied(horizontalPosition) && world.getOccupant(horizontalPosition).get().getClass() != Stump.class;
        boolean verticalOccupied = world.isOccupied(verticalPosition) && world.getOccupant(verticalPosition).get().getClass() != Stump.class;

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
        Optional<Entity> dudeTarget = findDudeTarget(world);
        if (dudeTarget.isEmpty() || !move(world, dudeTarget.get(), scheduler) || !transformDude(world, scheduler, imageLibrary)) {
            scheduleBehavior(scheduler, world, imageLibrary);
        }
    }

    public Optional<Entity> findDudeTarget(World world) {
        List<Class<?>> potentialTargets;

        if (this.getResourceCount() == this.getResourceLimit()) {
            potentialTargets = List.of(House.class);
        } else {
            potentialTargets = List.of(Tree.class, Sapling.class);
        }

        return world.findNearest(this.getPosition(), potentialTargets);
    }

    public void updateImage() {
        this.setImageIndex(this.getImageIndex() + 1);
    }




    
}

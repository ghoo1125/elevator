package elevator;

import elevator.Model.Direction;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Elevator implements Runnable {

    private Direction direction;
    private int currentFloor;
    private final Set<Integer> inButton;
    private final Set<Integer> outUpButton;
    private final Set<Integer> outDownButton;

    public Elevator() {
        this.direction = Direction.HOLD;
        this.currentFloor = 1;
        this.inButton = new HashSet<>();
        this.outUpButton = new HashSet<>();
        this.outDownButton = new HashSet<>();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                System.out.printf("%s %d\n", direction, currentFloor);
                Thread.sleep(500);
            } catch (InterruptedException e) {
                break;
            }

            switch (direction) {
                case HOLD:
                    // already at the expected floor
                    inButton.remove(currentFloor);
                    outDownButton.remove(currentFloor);
                    outUpButton.remove(currentFloor);

                    if (!inButton.isEmpty() || !outUpButton.isEmpty() || !outDownButton.isEmpty()) {
                        Stream.of(inButton, outUpButton, outDownButton)
                                .flatMap(Set::stream)
                                .findAny()
                                .ifPresent(targetFloor -> direction = targetFloor > currentFloor
                                        ? Direction.UP
                                        : Direction.DOWN);
                    }
                    break;
                case UP:
                    currentFloor++;
                    openOnButtonMatch();
                    var inAboveButtonEmpty = inButton.stream()
                            .filter(b -> b > currentFloor)
                            .collect(Collectors.toSet())
                            .isEmpty();
                    var outAboveButtonEmpty = Stream.of(outUpButton, outDownButton)
                            .flatMap(Set::stream)
                            .filter(b -> b > currentFloor)
                            .collect(Collectors.toSet())
                            .isEmpty();
                    if (inAboveButtonEmpty && outAboveButtonEmpty) {
                        direction = Direction.HOLD;
                    }
                    break;
                case DOWN:
                    currentFloor--;
                    openOnButtonMatch();
                    var inBelowButtonEmpty = inButton.stream()
                            .filter(b -> b < currentFloor)
                            .collect(Collectors.toSet())
                            .isEmpty();
                    var outBelowButtonEmpty = Stream.of(outUpButton, outDownButton)
                            .flatMap(Set::stream)
                            .filter(b -> b < currentFloor)
                            .collect(Collectors.toSet())
                            .isEmpty();
                    if (inBelowButtonEmpty && outBelowButtonEmpty) {
                        direction = Direction.HOLD;
                    }
                    break;
                default:
                    throw new RuntimeException("unexpected direction state");
            }
        }
    }

    public void inClick(Integer floor) {
        inButton.add(floor);
    }

    public void outUpClick(Integer floor) {
        outUpButton.add(floor);
    }

    public void outDownClick(Integer floor) {
        outDownButton.add(floor);
    }

    private void openOnButtonMatch() {
        if (inButton.contains(currentFloor)
                || (direction == Direction.UP && outUpButton.contains(currentFloor)
                || (direction == Direction.DOWN && outDownButton.contains(currentFloor)))) {
            System.out.println(currentFloor + " hodor!!!");
            inButton.remove(currentFloor);
            if (direction == Direction.UP) {
                outUpButton.remove(currentFloor);
            }
            if (direction == Direction.DOWN) {
                outDownButton.remove(currentFloor);
            }
        }
    }
}

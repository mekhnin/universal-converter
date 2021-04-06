package com.example.universalconverter.service;

import com.example.universalconverter.model.Graph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;

import java.util.*;
import java.util.stream.Stream;

@Service
public class ConverterImpl implements Converter {

    private final MathContext OUTPUT_MATH_CONTEXT = new MathContext(15);
    private final MathContext INTERMEDIATE_MATH_CONTEXT = MathContext.DECIMAL128;
    private final String WRONG_PATTERN = "^\\s*/.*|.*/.*/.*|.*/\\s*$|^\\s*\\*.*|.*\\*\\s*\\*.*|.*\\*\\s*$";

    private final Graph graph;

    @Autowired
    public ConverterImpl(Graph graph) {
        this.graph = graph;
    }

    private static class ResultKeeper {

        final String from;
        final String to;

        Queue<String> numerator;
        Queue<String> denominator;
        BigDecimal result;

        ResultKeeper(String from, String to) {
            this.from = from;
            this.to = to;
        }

    }

    public ResponseEntity<String> convert(String from, String to) {
        ResultKeeper keeper = new ResultKeeper(from, to);
        if (!checkInput(keeper)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!findRate(keeper)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(prepareAnswer(keeper), HttpStatus.OK);
    }

    private boolean checkInput(ResultKeeper keeper) {
        return checkFormat(keeper) && checkData(keeper);
    }

    private boolean checkFormat(ResultKeeper keeper) {
        return Stream.of(keeper.from, keeper.to).noneMatch(s -> s.matches(WRONG_PATTERN));
    }

    private boolean checkData(ResultKeeper keeper) {
        return prepareQueue(keeper) &&
                keeper.numerator.size() == keeper.denominator.size() &&
                keeper.numerator.size() > 0;
    }

    private boolean prepareQueue(ResultKeeper keeper) {
        String[] splitFrom = keeper.from.split("/");
        String[] splitTo = keeper.to.split("/");
        keeper.numerator = new ArrayDeque<>();
        keeper.denominator = new ArrayDeque<>();
        return addUnitsToQueue(splitFrom[0], keeper.numerator) &&
                addUnitsToQueue(splitTo[0], keeper.denominator) &&
                (splitFrom.length == 1 || addUnitsToQueue(splitFrom[1], keeper.denominator)) &&
                (splitTo.length == 1 || addUnitsToQueue(splitTo[1], keeper.numerator));
    }

    private boolean addUnitsToQueue(String input, Queue<String> output) {
        return Stream.of(input.split("\\*"))
                .map(String::trim)
                .filter(s -> !"".equals(s) && !"1".equals(s))
                .peek(output::add)
                .allMatch(s -> graph.getBonds(s) != null);
    }

    private boolean findRate(ResultKeeper keeper) {
        keeper.result = BigDecimal.ONE;
        while (!keeper.numerator.isEmpty()) {
            String from = keeper.numerator.poll();
            if (keeper.denominator.remove(from)) {
                continue;
            }
            Iterator<String> iterator = keeper.denominator.iterator();
            while (iterator.hasNext()) {
                String to = iterator.next();
                if (graph.getBonds(from).containsKey(to)) {
                    updateResult(from, to, keeper);
                    iterator.remove();
                    break;
                } else if (calculateRate(findPath(from, to), keeper)) {
                    iterator.remove();
                    break;
                }
            }
        }
        return keeper.denominator.isEmpty();
    }

    private void updateResult(String from, String to, ResultKeeper keeper) {
        double rate = graph.getBonds(from).get(to);
        keeper.result = keeper.result.multiply(BigDecimal.valueOf(rate), INTERMEDIATE_MATH_CONTEXT);
    }

    private List<String> findPath(String start, String destination) {
        List<String> route = new LinkedList<>();
        Deque<String> entryOrder = new ArrayDeque<>();
        Set<String> added = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Map<Integer, String> exitOrder = new HashMap<>();
        int exitIndex = 0;
        int destinationIndex = 0;
        boolean success = false;
        entryOrder.push(start);
        added.add(start);
        while (!entryOrder.isEmpty()) {
            String current = entryOrder.peek();
            if (!success && destination.equals(current)) {
                success = true;
                visited.add(current);
                destinationIndex = exitIndex + 1;
            }
            boolean hasChild = false;
            if (!success) {
                visited.add(current);
                for (String neighbor : graph.getBonds(current).keySet()) {
                    if (added.add(neighbor)) {
                        entryOrder.push(neighbor);
                        hasChild = true;
                    }
                }
            }
            if (!hasChild) {
                entryOrder.pop();
                if (visited.contains(current)) exitOrder.put(++exitIndex, current);
            }
        }
        for (int i = exitIndex; i >= destinationIndex; i--) {
            route.add(exitOrder.get(i));
        }
        return success ? route : Collections.emptyList();
    }

    private boolean calculateRate(List<String> route, ResultKeeper keeper) {
        int size = route.size();
        for (int i = 0; i <= size - 2; i++) {
            updateResult(route.get(i), route.get(i + 1), keeper);
        }
        return size > 1;
    }

    private String prepareAnswer(ResultKeeper keeper) {
        keeper.result = BigDecimal.ONE.multiply(keeper.result, OUTPUT_MATH_CONTEXT);
        String result = keeper.result.toPlainString();
        return result.replaceFirst("0*$", "").replaceFirst("\\.$", "");
    }

}

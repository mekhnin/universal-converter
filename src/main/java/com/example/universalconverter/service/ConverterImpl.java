package com.example.universalconverter.service;

import com.example.universalconverter.model.Graph;
import com.example.universalconverter.model.RequestObject;
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

    private final ThreadLocal<String> from = new ThreadLocal<>();
    private final ThreadLocal<String> to = new ThreadLocal<>();
    private final ThreadLocal<Queue<String>> numerator = new ThreadLocal<>();
    private final ThreadLocal<Queue<String>> denominator = new ThreadLocal<>();
    private final ThreadLocal<BigDecimal> result = new ThreadLocal<>();

    private final Graph graph;

    @Autowired
    public ConverterImpl(Graph graph) {
        this.graph = graph;
    }

    public ResponseEntity<String> convert(RequestObject object) {
        from.set(object.getFrom());
        to.set(object.getTo());
        if (!checkFormat() || !prepareData()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!findRate()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(prepareAnswer(), HttpStatus.OK);
    }

    private boolean checkFormat() {
        return Stream.of(from.get(), to.get()).noneMatch(s -> s.matches(WRONG_PATTERN));
    }

    private boolean prepareData() {
        numerator.set(new ArrayDeque<>());
        denominator.set(new ArrayDeque<>());
        String[] splitFrom = from.get().split("/");
        String[] splitTo = to.get().split("/");
        return addUnitsToQueue(numerator.get(), splitFrom[0], splitTo.length == 2 ? splitTo[1] : "") &&
                addUnitsToQueue(denominator.get(), splitTo[0], splitFrom.length == 2 ? splitFrom[1] : "") &&
                numerator.get().size() == denominator.get().size() &&
                !numerator.get().isEmpty();

    }

    private boolean addUnitsToQueue(Queue<String> output, String... input) {
        return Stream.of(input)
                .flatMap(s -> Arrays.stream(s.split("\\*")))
                .map(String::trim)
                .filter(s -> !"".equals(s) && !"1".equals(s))
                .peek(output::add)
                .allMatch(s -> graph.getBonds(s) != null);
    }

    private boolean findRate() {
        result.set(BigDecimal.ONE);
        while (!numerator.get().isEmpty()) {
            String from = numerator.get().poll();
            if (denominator.get().remove(from)) {
                continue;
            }
            Iterator<String> iterator = denominator.get().iterator();
            while (iterator.hasNext()) {
                String to = iterator.next();
                if (graph.getBonds(from).containsKey(to)) {
                    updateResult(from, to);
                    iterator.remove();
                    break;
                } else if (calculateRate(findPath(from, to))) {
                    iterator.remove();
                    break;
                }
            }
        }
        return denominator.get().isEmpty();
    }

    private void updateResult(String from, String to) {
        double rate = graph.getBonds(from).get(to);
        result.set(result.get().multiply(BigDecimal.valueOf(rate), INTERMEDIATE_MATH_CONTEXT));
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

    private boolean calculateRate(List<String> route) {
        int size = route.size();
        for (int i = 0; i <= size - 2; i++) {
            updateResult(route.get(i), route.get(i + 1));
        }
        return size > 1;
    }

    private String prepareAnswer() {
        String answer = result.get().round(OUTPUT_MATH_CONTEXT).toPlainString();
        return answer.replaceFirst("0*$", "").replaceFirst("\\.$", "");
    }

}
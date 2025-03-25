package ktu.kaganndemirr.util;

import ktu.kaganndemirr.message.UnicastCandidate;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UnicastCandidateSortingMethods {
    public static List<UnicastCandidate> sortUnicastCandidateListForDeadlineAscending(List<UnicastCandidate> unicastCandidateList) {
        return unicastCandidateList.stream()
                .sorted(Comparator.comparingInt(unicastCandidate -> unicastCandidate.getApplication().getDeadline()))
                .collect(Collectors.toList());
    }
}

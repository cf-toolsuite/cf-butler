package io.pivotal.cfapp.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class AppMetrics {

    private static LocalDate NOW = LocalDate.now();

    private List<AppDetail> detail;

    public AppMetrics(List<AppDetail> detail) {
        this.detail = detail;
    }

    public static String pushHeaders() {
        return "last pushed,application total";
    }

    public static String instanceStateHeaders() {
        return "state,instance total";
    }
    public Integer totalApplications() {
        return detail.size();
    }

    public Integer totalApplicationInstances() {
        return detail.stream().mapToInt(i -> i.getTotalInstances()).sum();
    }

    public Integer totalStartedInstances() {
        return detail.stream().filter(i -> i.getRequestedState().equals("started")).mapToInt(i -> i.getTotalInstances()).sum();
    }

    public Integer totalStoppedInstances() {
        return detail.stream().filter(i -> i.getRequestedState().equals("stopped")).mapToInt(i -> i.getTotalInstances()).sum();
    }

    public Integer pushedInLastDay() {
        return detail
                .stream()
                    .filter(
                        i -> i.getLastPushed() != null &&
                        ChronoUnit.DAYS.between(i.getLastPushed().toLocalDate(), NOW) <= 1
                    )
                    .collect(Collectors.toList())
                    .size();
    }

    public Integer pushedInLastWeek() {
        return detail
                .stream()
                    .filter(
                        i -> i.getLastPushed() != null &&
                        ChronoUnit.WEEKS.between(i.getLastPushed().toLocalDate(), NOW) <= 1 &&
                        ChronoUnit.DAYS.between(i.getLastPushed().toLocalDate(), NOW) > 1
                    )
                    .collect(Collectors.toList())
                    .size();
    }

    public Integer pushedInLastMonth() {
        return detail
                .stream()
                    .filter(
                        i -> i.getLastPushed() != null &&
                        ChronoUnit.MONTHS.between(i.getLastPushed().toLocalDate(), NOW) <= 1 &&
                        ChronoUnit.WEEKS.between(i.getLastPushed().toLocalDate(), NOW) > 1
                    )
                    .collect(Collectors.toList())
                    .size();
    }

    public Integer pushedInLastThreeMonths() {
        return detail
                .stream()
                    .filter(
                        i -> i.getLastPushed() != null &&
                        ChronoUnit.MONTHS.between(i.getLastPushed().toLocalDate(), NOW) <= 3 &&
                        ChronoUnit.MONTHS.between(i.getLastPushed().toLocalDate(), NOW) > 1
                    )
                    .collect(Collectors.toList())
                    .size();
    }

    public Integer pushedInLastSixMonths() {
        return detail
                .stream()
                    .filter(
                        i -> i.getLastPushed() != null &&
                        ChronoUnit.MONTHS.between(i.getLastPushed().toLocalDate(), NOW) <= 6 &&
                        ChronoUnit.MONTHS.between(i.getLastPushed().toLocalDate(), NOW) > 3
                    )
                    .collect(Collectors.toList())
                    .size();
    }

    public Integer pushedInLastYear() {
        return detail
                .stream()
                    .filter(
                        i -> i.getLastPushed() != null &&
                        ChronoUnit.YEARS.between(i.getLastPushed().toLocalDate(), NOW) <= 1 &&
                        ChronoUnit.MONTHS.between(i.getLastPushed().toLocalDate(), NOW) > 6
                    )
                    .collect(Collectors.toList())
                    .size();
    }

    public Integer pushedBeyondOneYear() {
        return detail
                .stream()
                    .filter(
                        i -> i.getLastPushed() != null &&
                        ChronoUnit.YEARS.between(i.getLastPushed().toLocalDate(), NOW) > 1
                    )
                    .collect(Collectors.toList())
                    .size();
    }
}

package com.teamtreehouse.jobs;

import com.teamtreehouse.jobs.model.Job;
import com.teamtreehouse.jobs.service.JobService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class App {

    public static void main(String[] args) {
        JobService service = new JobService();
        boolean shouldRefresh = false;
        try {
            if (shouldRefresh) {
                service.refresh();
            }
            List<Job> jobs = service.loadJobs();
            System.out.printf("Total jobs:  %d %n %n", jobs.size());
            explore(jobs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void explore(List<Job> jobs) {
        // Your amazing code below...

        Function<String, String> converter = createDateStringConverter(
                DateTimeFormatter.RFC_1123_DATE_TIME,
                DateTimeFormatter.ISO_DATE_TIME);

        jobs.stream()
                .map(Job::getDateTimeString)
                .map(converter)
                .limit(5)
                .collect(Collectors.toList())
                .forEach(System.out::println);
    }

    private static Function<String, String> createDateStringConverter(
            DateTimeFormatter inFormatter,
            DateTimeFormatter outFormatter) {
        return dateString -> {
            return LocalDateTime.parse(dateString, inFormatter).format(outFormatter);
        };
    }

    private static void checkForJobs(List<Job> jobs) {
        Job first = jobs.get(0);
        System.out.println("First job: " + first);
        Predicate<Job> caJobChecker = job -> job.getState().equals("CA");

        Job caJob = jobs.stream()
                .filter(caJobChecker)
                .findFirst()
                .orElseThrow(NullPointerException::new);

        emailIfMatches(caJob, caJobChecker.and(App::isJuniorJob));
    }

    private static void changeDateFormat(List<Job> jobs) {

        Function<String, LocalDateTime> indeedDateConverter =
                dateString -> LocalDateTime.parse(
                        dateString,
                        DateTimeFormatter.RFC_1123_DATE_TIME);

        Function<LocalDateTime, String> siteDateStringConverter =
                date -> date.format(DateTimeFormatter.ofPattern("M / d / YY"));

        Function<String, String> indeedToSiteDateStringConverter = indeedDateConverter.andThen(siteDateStringConverter);

        jobs.stream()
                .map(Job::getDateTimeString)
                .map(indeedToSiteDateStringConverter)
                .limit(5)
                .collect(Collectors.toList())
                .forEach(System.out::println);
    }


    private static void emailIfMatches(Job job, Predicate<Job> checker) {
        if (checker.test(job)) {
            System.out.println("I am sending an email about " + job);
        }
    }

    private static void displayCompanyNamesThatStartWith(List<Job> jobs, String startsWith) {
        jobs.stream()
                .map(Job::getCompany)
                .distinct()
                .sorted()
                .peek(company -> System.out.println("---" + company))
                .filter(company -> company.startsWith(startsWith))
                .forEach(System.out::println);
    }

    private static void foundJob(List<Job> jobs, String searchTerm) {
        Optional<Job> foundJob = luckySearchJob(jobs, searchTerm);
        System.out.println(foundJob
                .map(Job::getTitle)
                .orElse("No job found"));
    }


    private static void displayCompaniesMenu(List<Job> jobs) {
        List<String> companies = jobs.stream()
                .map(Job::getCompany)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        displayCompaniesMenuUsingIterate(companies);
    }

    private static void displayCompaniesMenuUsingIterate(List<String> companies) {
        int pageSize = 20;
        int numPages = companies.size() / pageSize;

        IntStream.iterate(1, i -> i + pageSize)
                .mapToObj(i -> String.format("%d. %s", i, companies.get(i - 1)))
                .limit(numPages)
                .forEach(System.out::println);
    }

    private static void displayCompaniesMenuUsingRange(List<String> companies) {
        IntStream.rangeClosed(1, 20)
                .mapToObj(i -> String.format("%02d. %s", i, companies.get(i - 1)))
                .forEach(System.out::println);
    }

    private static void displayCompaniesMenuImperatively(List<String> companies) {
        for (int i = 0; i < 20; i++) {
            System.out.printf("%02d. %s %n", i + 1, companies.get(i));
        }
    }

    private static Optional<Job> luckySearchJob(List<Job> jobs, String searchTerm) {
        return jobs.stream()
                .filter(job -> job.getTitle().contains(searchTerm))
                .findFirst();
    }

    private static Optional getLongestCompanyName(List<Job> jobs) {
        return jobs.stream()
                .map(Job::getCompany)
                .max(Comparator.comparingInt(String::length));
    }

    private static OptionalDouble getAverageCompanyNameLength(List<Job> jobs) {
        return jobs.stream()
                .map(Job::getCompany)
                .mapToInt(String::length)
                .average();
    }

    private static void printSnippetWordCounts(Map<String, Long> wordCounts) {
        wordCounts.forEach(
                (key, value) -> System.out.printf("%s occurs %d times %n", key, value));
    }

    private static Map<String, Long> getSnippetWordCountsStream(List<Job> jobs) {
        return jobs.stream()
                .map(Job::getSnippet)
                .map(snippet -> snippet.split("\\W+"))
                .flatMap(Stream::of)
                .filter(word -> word.length() > 0)
                .map(String::toLowerCase)
                .collect(Collectors.groupingBy(
                        Function.identity(), Collectors.counting()
                ));
    }

    private static boolean isJuniorJob(Job job) {
        String title = job.getTitle().toLowerCase();
        return (title.contains("junior") || title.contains("jr"));
    }

    private static List<Job> getThreeJuniorJobsStream(List<Job> jobs) {
        return jobs.stream()
                .filter(App::isJuniorJob)
                .limit(3)
                .collect(Collectors.toList());
    }

    private static List<Job> getThreeJuniorJobsImperatively(List<Job> jobs) {
        List<Job> juniorJobs = new ArrayList<>();
        for (Job job : jobs) {
            if (isJuniorJob(job)) {
                juniorJobs.add(job);
                if (juniorJobs.size() >= 3) {
                    break;
                }
            }
        }
        return juniorJobs;
    }

    private static List<String> getCaptionsStream(List<Job> jobs) {
        return jobs.stream()
                .filter(App::isJuniorJob)
                .map(Job::getCaption)
                .limit(3)
                .collect(Collectors.toList());
    }

    private static List<String> getCaptionsImperatively(List<Job> jobs) {
        List<String> captions = new ArrayList<>();
        for (Job job : jobs) {
            if (isJuniorJob(job)) {
                captions.add(job.getCaption());
                if (captions.size() >= 3) {
                    break;
                }
            }
        }
        return captions;
    }

    private static void printPortlandJobsStream(List<Job> jobs) {
        jobs.stream()
                .filter(job -> job.getState().equals("OR"))
                .filter(job -> job.getCity().equals("Portland"))
                .forEach(System.out::println);
    }

    private static void printPortlandJobsImperatively(List<Job> jobs) {
        for (Job job : jobs) {
            if (job.getState().equals("OR") && job.getCity().equals("Portland")) {
                System.out.println(job);
            }
        }
    }
}

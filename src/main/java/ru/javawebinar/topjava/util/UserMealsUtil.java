package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExceed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> mealList = Arrays.asList(
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,10,0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,13,0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,20,0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,10,0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,13,0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,20,0), "Ужин", 510)
        );
        List<UserMealWithExceed> filteredMealsWithExceed = getFilteredWithExceeded(mealList, LocalTime.of(7, 0), LocalTime.of(12,0), 2000);
        filteredMealsWithExceed.forEach(System.out::println);

        List<UserMealWithExceed> filteredMealsWithExceedByCycle = getFilteredWithExceededByCycle(mealList, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        filteredMealsWithExceedByCycle.forEach(System.out::println);

        List<UserMealWithExceed> filteredMealsWithExceedOnce = getFilteredWithExceededOnce(mealList, LocalTime.of( 7, 0), LocalTime.of(12, 0), 2000);
        filteredMealsWithExceedOnce.forEach(System.out::println);

//        .toLocalDate();
//        .toLocalTime();
    }

    /** HW0 -> Stream API **/
    public static List<UserMealWithExceed>  getFilteredWithExceeded(List<UserMeal> mealList, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        // TODO return filtered list with correctly exceeded field using Stream API
        Map<LocalDate, Integer> caloriesSumByDate = mealList.stream().collect(Collectors.groupingBy(um -> um.getDateTime().toLocalDate(), Collectors.summingInt(UserMeal::getCalories)));

        return mealList.stream()
                .filter(um->TimeUtil.isBetween(um.getDateTime().toLocalTime(), startTime, endTime))
                .map(um-> new UserMealWithExceed(um.getDateTime(), um.getDescription(), um.getCalories(),
                        caloriesSumByDate.get(um.getDateTime().toLocalDate())>caloriesPerDay))
                .collect(Collectors.toList());
    }

    /** HW0 -> Optional **/
    public static List<UserMealWithExceed>  getFilteredWithExceededByCycle(List<UserMeal> mealList, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        // TODO return filtered list with correctly exceeded field through cycles
        Map<LocalDate, Integer> caloriesSumPerDay = new HashMap<>();
        for (UserMeal meal : mealList) {
            LocalDate mealDate = meal.getDateTime().toLocalDate();
            caloriesSumPerDay.put(mealDate, caloriesSumPerDay.getOrDefault(mealDate, 0) + meal.getCalories());
        }

        List<UserMealWithExceed> mealExceeded = new ArrayList<>();
        for (UserMeal meal : mealList) {
            LocalDateTime mealDate = meal.getDateTime();
            if (TimeUtil.isBetween(mealDate.toLocalTime(), startTime, endTime)) {
                mealExceeded.add(new UserMealWithExceed(mealDate, meal.getDescription(), meal.getCalories(),
                        caloriesSumPerDay.get(mealDate.toLocalDate()) > caloriesPerDay));
            }
        }
        return mealExceeded;
    }

    /** HW0 -> Optional 2 **/
    public static List<UserMealWithExceed> getFilteredWithExceededOnce(List<UserMeal> mealList, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        // TODO return filtered list with correctly exceeded field through one cycle
        return mealList.stream()
                .collect(Collectors.groupingBy(UserMeal::getDateTime)).values().stream().flatMap(dayMeals -> {
                    boolean exceed = dayMeals.stream().mapToInt(UserMeal::getCalories).sum() > caloriesPerDay;
                    return dayMeals.stream().filter(meal ->
                            TimeUtil.isBetween(meal.getDateTime().toLocalTime(), startTime, endTime))
                            .map(meal -> createWithExceed(meal, exceed));
        })
                .collect(Collectors.toList());
    }

    public static UserMealWithExceed createWithExceed(UserMeal meal, boolean exceeded) {
        return new UserMealWithExceed(meal.getDateTime(), meal.getDescription(), meal.getCalories(), exceeded);
    }
}

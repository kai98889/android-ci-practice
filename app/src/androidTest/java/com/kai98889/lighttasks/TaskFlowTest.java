package com.kai98889.lighttasks;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class TaskFlowTest {
    @Rule public ActivityScenarioRule<MainActivity> activity = new ActivityScenarioRule<>(MainActivity.class);
    @Test public void addCompleteReopenAndDelete() {
        String task = "Smoke " + System.currentTimeMillis();
        onView(withHint(R.string.input_hint)).perform(typeText(task), closeSoftKeyboard());
        onView(withText(R.string.add)).perform(click());
        onView(withText(task)).check(matches(isDisplayed())).perform(click());
        onView(withText(task)).check(matches(isChecked()));
        activity.getScenario().recreate();
        onView(withText(task)).check(matches(isChecked()));
        onView(withContentDescription("删除任务：" + task)).perform(click());
        onView(withText(R.string.empty)).check(matches(isDisplayed()));
    }
}

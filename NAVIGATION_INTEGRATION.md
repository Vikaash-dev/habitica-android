# Navigation Integration for Contest Tracking

This guide explains how to integrate the Contest Tracking feature into the main app navigation.

## Adding ContestsFragment to Navigation Graph

Add the following to `Habitica/res/navigation/navigation.xml`:

```xml
<fragment
    android:id="@+id/contestsFragment"
    android:name="com.habitrpg.android.habitica.ui.fragments.contests.ContestsFragment"
    android:label="Contests">
    <deepLink app:uri="habitica.com/contests" />
</fragment>

<activity
    android:id="@+id/contestFormActivity"
    android:name="com.habitrpg.android.habitica.ui.activities.ContestFormActivity">
    <argument
        android:name="contest_id"
        app:argType="string"
        app:nullable="true" />
</activity>
```

## Adding Navigation Menu Item

### Option 1: Add to Main Bottom Navigation

Add to the bottom navigation menu (if you want it as a main tab):

1. Open `Habitica/res/menu/menu_bottom_navigation.xml` (or equivalent)
2. Add a new menu item:

```xml
<item
    android:id="@+id/contestsFragment"
    android:icon="@drawable/ic_contest"
    android:title="Contests" />
```

### Option 2: Add to Sidebar/Drawer Navigation

Add to the drawer navigation menu:

1. Open `Habitica/res/menu/drawer_main.xml` (or equivalent)
2. Add a new menu item in an appropriate section:

```xml
<item
    android:id="@+id/contestsFragment"
    android:icon="@drawable/ic_contest"
    android:title="Contests" />
```

### Option 3: Add as a Feature Button

Add a button in the main activity or tasks view that navigates to contests:

```kotlin
binding.contestsButton.setOnClickListener {
    findNavController().navigate(R.id.contestsFragment)
}
```

## Programmatic Navigation

To navigate to the Contests fragment from code:

```kotlin
// Using Navigation Component
findNavController().navigate(R.id.contestsFragment)

// Or with arguments
val bundle = Bundle().apply {
    // Add any arguments if needed
}
findNavController().navigate(R.id.contestsFragment, bundle)
```

## Deep Links

The contest tracking feature supports deep linking:

- `habitica.com/contests` - Opens the contests list

To test deep links:

```bash
adb shell am start -a android.intent.action.VIEW -d "https://habitica.com/contests"
```

## Icon Resource

You'll need to add an icon for the contests feature. Place it in:
- `Habitica/res/drawable/ic_contest.xml`

Example icon (Material Design trophy/award icon):

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FF000000"
        android:pathData="M7,2L7,13L12,18L17,13L17,2L7,2ZM9,4L15,4L15,11.17L12,14.17L9,11.17L9,4ZM4,4L4,6L6,6L6,4L4,4ZM18,4L18,6L20,6L20,4L18,4ZM4,8L4,10L6,10L6,8L4,8ZM18,8L18,10L20,10L20,8L18,8ZM4,14L4,16L6,16L6,14L4,14ZM18,14L18,16L20,16L20,14L18,14ZM9,18L9,20L15,20L15,18L9,18Z"/>
</vector>
```

## MainActivity Integration

If using the drawer/bottom navigation, make sure your MainActivity handles the navigation:

```kotlin
// In MainActivity.kt
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // ... existing code ...
    
    // Set up navigation
    setupNavigation()
}

private fun setupNavigation() {
    binding.bottomNavigation.setOnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.contestsFragment -> {
                navController.navigate(R.id.contestsFragment)
                true
            }
            // ... other items ...
            else -> false
        }
    }
}
```

## Testing the Integration

1. Build and run the app
2. Navigate using the new menu item or button
3. Verify the ContestsFragment displays correctly
4. Test navigation to ContestFormActivity by:
   - Clicking the FAB in ContestsFragment
   - Clicking on an existing contest
5. Test deep linking (if implemented)

## Troubleshooting

### Fragment not found
- Ensure the fragment package name is correct in navigation.xml
- Clean and rebuild the project

### Navigation crashes
- Check that all required dependencies are in build.gradle
- Verify fragment IDs match between navigation.xml and code

### Icon not displaying
- Ensure icon resource exists in drawable folder
- Use vector drawables for better compatibility
- Check icon resource name matches menu XML reference

## Future Enhancements

Consider adding:
- Badge showing count of upcoming contests
- Notification dot for contests starting soon
- Quick action to add contest from main screen
- Widget showing next contest

# Contest Tracking Feature

This feature adds contest tracking functionality to the Habitica Android app, allowing users to track coding contests from platforms like Codeforces, LeetCode, CodeChef, and others using Habitica's gamification approach.

## Overview

The Contest Tracking feature integrates seamlessly with Habitica's existing task management and gamification system. Users can:

- Track upcoming coding contests from various platforms
- Monitor ongoing contests they're participating in
- Review past contest performance
- Earn XP and gold based on contest performance (similar to completing tasks)
- View detailed contest information including platform, timing, and results

## Architecture

### Data Layer

**Models** (`models/contests/`)
- `Contest.kt`: Main contest model with Realm support
  - Contains contest details (name, platform, URL, timing)
  - Tracks participation and results (rank, problems solved, score)
  - Integrates with Habitica's reward system (XP and gold)
  - Provides utility methods to check contest status (upcoming/ongoing/past)

- `ContestList.kt`: Container for multiple contests

**Repositories**
- `ContestRepository.kt`: Interface defining contest data operations
- `ContestRepositoryImpl.kt`: Implementation handling local and remote data
- `ContestLocalRepository.kt`: Interface for local storage operations
- `RealmContestLocalRepository.kt`: Realm-based local storage implementation

### UI Layer

**ViewModels** (`ui/viewmodels/`)
- `ContestsViewModel.kt`: Manages contest state and business logic
  - Exposes StateFlows for upcoming, ongoing, and past contests
  - Handles CRUD operations for contests
  - Manages loading states and error handling

**Fragments** (`ui/fragments/contests/`)
- `ContestsFragment.kt`: Main fragment displaying contests in three sections
  - Upcoming Contests: Shows future contests sorted by start time
  - Ongoing Contests: Displays currently active contests
  - Past Contests: Lists completed contests with results

**Activities** (`ui/activities/`)
- `ContestFormActivity.kt`: Form for creating and editing contests
  - Fields for contest name, platform, description, URL, timing, and duration
  - Date/time pickers for start and end times
  - Platform selection spinner

**Adapters** (`ui/adapter/contests/`)
- `ContestsAdapter.kt`: RecyclerView adapter for displaying contest items
  - Shows contest name, platform, date, status
  - Displays progress for participated contests

### Resources

**Layouts** (`res/layout/`)
- `fragment_contests.xml`: Main fragment layout with three RecyclerViews
- `item_contest.xml`: Contest card layout with Material Design
- `activity_contest_form.xml`: Contest form with input fields

**Menus** (`res/menu/`)
- `menu_contests.xml`: Menu with refresh and add contest actions

## Integration Points

### Dependency Injection
The Contest repositories are registered in `UserRepositoryModule.kt`:
```kotlin
@Provides
fun providesContestLocalRepository(realm: Realm): ContestLocalRepository

@Provides
fun providesContestRepository(
    localRepository: ContestLocalRepository,
    apiClient: ApiClient,
    authenticationHandler: AuthenticationHandler
): ContestRepository
```

### AndroidManifest
The `ContestFormActivity` is registered in `AndroidManifest.xml`:
```xml
<activity
    android:name=".ui.activities.ContestFormActivity"
    android:parentActivityName=".ui.activities.MainActivity"
    ...
/>
```

## Usage

### Adding a Contest
1. Navigate to the Contests section
2. Tap the floating action button (+) or use the menu
3. Fill in contest details:
   - Name (required)
   - Platform (Codeforces, LeetCode, CodeChef, etc.)
   - Description
   - URL
   - Start and end times
   - Duration
4. Save the contest

### Tracking Contest Performance
1. Mark a contest as participated
2. After the contest, update results:
   - Rank achieved
   - Problems solved
   - Total problems
   - Score
3. Complete the contest to claim rewards

### Earning Rewards
Rewards are calculated based on:
- **Base rewards**: 50 XP and 10 gold for participation
- **Completion bonus**: Based on percentage of problems solved
- **Rank bonus**:
  - Rank 1-10: +200 XP, +50 gold
  - Rank 11-50: +100 XP, +25 gold
  - Rank 51-100: +50 XP, +10 gold

## Future Enhancements

Potential improvements for this feature:

1. **API Integration**: Automatically fetch contests from platform APIs
   - Codeforces API
   - LeetCode API
   - CodeChef API

2. **Reminders**: Notify users before contests start

3. **Statistics**: Track contest performance over time
   - Win rate
   - Average rank
   - Improvement trends

4. **Social Features**: 
   - Share contest results with party/guild
   - Compare performance with friends
   - Contest leaderboards

5. **Integration with Habitica Tasks**:
   - Automatically create dailies for upcoming contests
   - Convert contest participation into challenge progress

6. **Contest Calendar**: Visual calendar view of contests

7. **Platform-Specific Features**:
   - Rating changes for rated contests
   - Problem difficulty distribution
   - Contest categories/types

## Code Style

The implementation follows Habitica's coding standards:
- Uses Kotlin idioms and coroutines
- Follows the repository pattern
- Implements Material Design guidelines
- Uses Hilt for dependency injection
- Uses Realm for local data persistence
- Follows existing architectural patterns

## Testing

To test the feature:

1. Build and run the app
2. Navigate to the Contests section (requires navigation integration)
3. Add a test contest with upcoming time
4. Verify it appears in the "Upcoming" section
5. Edit the contest to have a past end time
6. Verify it moves to the "Past" section
7. Update results and complete the contest
8. Verify rewards are calculated correctly

## Notes

- The feature currently works with local data storage
- API integration for fetching contests requires backend support
- Reward claiming integration with user stats needs backend coordination
- Navigation to the ContestsFragment requires integration with the app's navigation system

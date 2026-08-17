# Walkthrough - Login and Signout Fixes

I have implemented fixes for the "Unexpected error" during login and the non-functional "Sign out" button. These changes improve error visibility and ensure reliable session management.

## Changes Made

### Login Flow Robustness
- **Specific Error Messages**: Updated `LoginViewModel` to show actual backend error messages (e.g., "Invalid password") instead of the generic "Unexpected error".
- **Detailed Diagnosis**: If the backend returns a success status but missing critical fields, the app now shows exactly what is missing (e.g., "Session token missing from server" or "reCAPTCHA verification required").
- **Flexible Field Parsing**: Added `@SerializedName` aliases (`mfa_required`, `challenge_token`) to `LoginResultDTO` to handle potential snake_case variations from different backend versions.

### Signout Reliability
- **Direct Navigation**: Wired the `onLogout` callback in `RootNavHost` to explicitly navigate to the Login screen. This ensures that clicking "Sign out" in the Profile tab or Sidebar provides immediate UI feedback.
- **Reactive Safety**: Refined the `isAuthenticated` listener in `RootNavHost` to act as a robust fallback for background logouts (e.g., when a refresh token expires), while avoiding redundant navigation calls.

## Verification Results

### Manual Verification
- **Login Errors**: Verified that entering wrong credentials now displays the specific error returned by the server.
- **Sign Out**: Verified that clicking the "Sign out" button in both the Profile tab and the Sidebar immediately redirects the user to the Login screen and clears the back stack.
- **Session State**: Confirmed that the app correctly identifies missing tokens and prompts the user accordingly.

> [!TIP]
> If you still encounter an "Unexpected error", the toast will now tell you exactly which field was missing from the server response, which will help in debugging the backend API.

> [!IMPORTANT]
> The signout button is now directly wired to navigation, which bypasses potential race conditions in the reactive state collector.

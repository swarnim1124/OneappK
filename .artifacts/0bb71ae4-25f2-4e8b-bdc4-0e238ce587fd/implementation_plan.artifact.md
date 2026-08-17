# Implementation Plan - Fix Login "Unexpected Error" and Signout Issue

Address the "Unexpected error occurred" during login by improving error visibility and robust field parsing, and fix the signout button functionality by ensuring reliable navigation.

## User Review Required

> [!IMPORTANT]
> Error messages during login will now be more specific, showing the actual backend error or indicating which fields were missing.
> The signout process will be made more direct to ensure navigation always occurs when the user clicks "Sign out".

## Proposed Changes

### [Component] Feature: Login (Fix Unexpected Error)

#### [MODIFY] [LoginViewModel.kt](file:///C:/Users/swarn/OneDrive/Desktop/OneappK-master-cleaned/OneappK-master/feature/login/src/main/java/com/xsc/oneapp/feature/login/ui/viewmodel/LoginViewModel.kt)
- Update `userFacingMessage` to show the `errorMessage` from `BusinessError` directly.
- In `performLogin` and `performMfaVerification`, replace generic "Unexpected error" with specific details:
    - If `result.captchaRequired` is true, show "reCAPTCHA verification required".
    - If `result.mfaRequired` is true but `challengeToken` is missing, show "MFA challenge token missing".
    - If status is success but `token` is missing, show "Session token missing from server".

#### [MODIFY] [LoginResponseDTOs.kt](file:///C:/Users/swarn/OneDrive/Desktop/OneappK-master-cleaned/OneappK-master/feature/login/src/main/java/com/xsc/oneapp/feature/login/data/remote/dto/LoginResponseDTOs.kt)
- Add snake_case aliases for `mfa_required` and `challenge_token` using `@SerializedName` to handle backend inconsistency.

---

### [Component] Feature: App Navigation (Fix Signout)

#### [MODIFY] [RootNavHost.kt](file:///C:/Users/swarn/OneDrive/Desktop/OneappK-master-cleaned/OneappK-master/app/src/main/java/com/xsc/oneapp/navigation/RootNavHost.kt)
- Update the `onLogout` callback in `DashboardScreen` to explicitly navigate to `LOGIN`. This provides a direct path for the UI button while keeping the reactive `isAuthenticated` listener as a safety for background/forced logouts.
- Refine the reactive `isAuthenticated` listener to ensure it doesn't miss transitions.

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/swarn/OneDrive/Desktop/OneappK-master-cleaned/OneappK-master/feature/dashboard/src/main/java/com/xsc/oneapp/feature/dashboard/ui/screen/DashboardScreen.kt)
- Ensure `onLogout` is correctly propagated through the `DashboardTab.PROFILE` and `SidebarView`.

## Verification Plan

### Manual Verification
- **Login Error**: Verify that incorrect credentials now show the specific error (e.g., "Invalid credentials") instead of "Unexpected error".
- **Successful Login**: Verify that login works and leads to the dashboard.
- **Sign Out**: Click the "Sign out" button in both the Profile tab and the Sidebar. Verify that the app navigates back to the Login screen immediately.
- **Forced Sign Out**: (Simulated) Clear tokens manually to verify that the reactive listener still works and redirects to Login.

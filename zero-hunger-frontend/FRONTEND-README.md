# ZeroHunger Frontend - Quick Reference

## Environment Setup

The frontend is now configured to work with the Jakarta EE backend running on GlassFish.

### Key Changes Made

1. **API URL Updated**

   - Changed from Laravel's `localhost:8000` to GlassFish's `localhost:8080`
   - Updated path to match WAR deployment: `/zero-hunger-1.0-SNAPSHOT/api/v1`

2. **Field Naming Convention**

   - ✅ `quantityKg` (Jakarta EE camelCase)
   - ❌ `quantity_kg` (Laravel snake_case)

   Updated fields:

   - `quantityKg`, `pickupCode`, `deliveryCode`
   - `expiresAt`, `createdAt`, `updatedAt`
   - `impactScore`, `pickedUpAt`, `deliveredAt`

3. **Response Handling**

   - Jakarta EE returns arrays/objects directly
   - Removed Laravel's `response.data.data` wrapper
   - Now uses: `Array.isArray(response.data) ? response.data : []`

4. **Roles as Set**
   - Backend returns `Set<String>` for roles
   - Converted to Array: `Array.from(user.roles || [])`

## Running the Application

### Start Backend (Jakarta EE)

```bash
# 1. Start MySQL
cd zero-hunger
docker-compose up -d

# 2. Deploy to GlassFish
# Build WAR and deploy via GlassFish Admin Console
# or use your IDE's GlassFish integration
```

### Start Frontend (Next.js)

```bash
cd laravel_php_p/zerohunger-frontend
npm install
npm run dev
```

Visit: http://localhost:3000

## Test Flow

1. **Register a Donor**

   - Go to registration (create this page or use API directly)
   - Role: "donor"
   - Email: donor@test.com

2. **Register a Volunteer**

   - Role: "volunteer"
   - Email: volunteer@test.com

3. **Donor Creates Donation**

   - Login as donor
   - Click "Create New Donation"
   - Fill form with location coordinates
   - Submit

4. **Volunteer Claims Donation**

   - Login as volunteer
   - See available donations
   - Click "Claim"
   - Receive pickup code

5. **Complete Flow**
   - Volunteer marks "Picked Up" (enter code)
   - Volunteer marks "Delivered"
   - Both users gain impact score points

## API Endpoints Reference

### Auth

- POST `/register` → `{message, user, token}`
- POST `/login` → `{message, user, token}`
- GET `/me` → `{user}`

### Donations

- GET `/donations` → `[...]` (array of donations)
- POST `/donations` → donation object
- POST `/donations/{id}/claim` → `{message, donation, pickup_code}`

### Claims

- GET `/claims` → `[...]` (array of claims)
- POST `/claims/{id}/pickup` → `{message, claim}`
- POST `/claims/{id}/deliver` → `{message, claim}`

## Common Issues

### "Network Error"

- ✅ Check GlassFish is running on port 8080
- ✅ Verify WAR is deployed correctly
- ✅ Check CORS filter is enabled

### "Unauthorized" / 401

- ✅ Token is stored in localStorage
- ✅ Bearer token format: `Bearer {token}`
- ✅ Check `/me` endpoint returns user

### Empty Dashboard

- ✅ User has correct role (donor/volunteer)
- ✅ API returns data (check Network tab)
- ✅ Response is not wrapped in `{data: []}`

### Roles Not Working

- ✅ Roles stored in `user_roles` table
- ✅ Frontend converts Set to Array
- ✅ Role check: `roles.includes('donor')`

## Files Modified

- ✅ `src/lib/api.js` - API URL and endpoints
- ✅ `src/app/dashboard/page.js` - Response handling, role checks, field names
- ✅ `src/app/login/page.js` - Error message
- ✅ `src/components/CreateDonationForm.js` - Field names to camelCase
- ✅ `.env.local` - Environment configuration (created)

## Next Features to Add

1. **Registration Page** - Currently missing, users must register via API
2. **Profile Page** - Update user location, phone, etc.
3. **Notifications** - Display notifications from backend
4. **Map Improvements** - Show user location, nearby donations
5. **Admin Dashboard** - Manage users, donations, claims
6. **Image Upload** - Add photos to donations
7. **Real-time Updates** - WebSocket for live notifications

## Development Tips

### Adding New Features

1. Check backend model for field names (camelCase)
2. Add API method to `api.js`
3. Handle response as direct array/object
4. Convert roles Set to Array when needed
5. Test with Network tab open in DevTools

### Debugging

```javascript
// Check API response structure
console.log("Response:", response.data);

// Check user roles
console.log("Roles:", Array.from(user.roles || []));

// Check form data before submit
console.log("Form data:", formData);
```

---

Last Updated: December 18, 2025

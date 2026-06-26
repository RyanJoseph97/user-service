# user-service

Manages user accounts, JWT authentication, social graph (follow/unfollow), notifications, and direct messages for EventMaster.

Port: `8080`. Context path: `/user-service`.

## Endpoints

### Users
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| `POST` | `/users` | None | Register a new user |
| `GET` | `/users` | Required | List all users |
| `GET` | `/users/search` | None | Search users by username |
| `GET` | `/users/{id}` | Required | Get user by ID |
| `GET` | `/users/by-username/{username}` | Required | Get user by username |
| `GET` | `/users/by-email/{email}` | Required | Get user by email |
| `PATCH` | `/users/{username}` | Required | Update profile |
| `PATCH` | `/users/{username}/password` | Required | Change password |
| `PATCH` | `/users/{username}/verify` | Admin | Set AccountStatus |
| `DELETE` | `/users/{username}` | Required | Delete account |

### Auth
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| `POST` | `/users/login` | None | Returns JWT |
| `POST` | `/users/token/refresh` | None | Refresh JWT |
| `POST` | `/users/logout` | None | Invalidate token |

### Follow
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| `POST` | `/users/{username}/follow` | Required | Follow a user (or send request if private) |
| `DELETE` | `/users/{username}/follow` | Required | Unfollow |
| `GET` | `/users/{username}/followers` | Required | List followers |
| `GET` | `/users/{username}/following` | Required | List followed accounts |
| `GET` | `/users/{username}/follow-requests` | Required | Pending incoming follow requests |
| `GET` | `/users/{username}/follow-request-status` | Required | Check request status |
| `POST` | `/users/{username}/follow-requests/{requesterUsername}/approve` | Required | Approve request |
| `DELETE` | `/users/{username}/follow-requests/{requesterUsername}` | Required | Reject request |

### Notifications
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| `GET` | `/users/notifications` | Required | List notifications for caller |
| `GET` | `/users/notifications/unseen-count` | Required | Count of unseen notifications |
| `POST` | `/users/notifications/mark-seen` | Required | Mark notifications as seen |

### Messages
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| `POST` | `/users/messages` | Required | Send a direct message |
| `GET` | `/users/messages/conversations` | Required | List conversations |
| `GET` | `/users/messages/{username}` | Required | Get thread with a user |

## Authentication

All endpoints except `POST /users`, `POST /users/login`, `POST /users/token/refresh`, `POST /users/logout`, and `GET /users/search` require a `Authorization: Bearer <token>` header. JWTs are issued by `POST /users/login` and are valid for 24 hours.

## Account Status

New accounts start as `UNVERIFIED`. An admin can advance status via `PATCH /users/{username}/verify`:

- `UNVERIFIED` — can create INVITE_ONLY events only
- `VERIFIED` — can create PUBLIC events
- `TRUSTED` — same as VERIFIED, higher trust tier

Admin username is controlled by the `ADMIN_USERNAME` environment variable (default: `admin`).

## Private Profiles

If a user's profile is set to private, follow requests must be approved before the requester appears in the follower list or can access protected content.

## Running Locally

```bash
cd user-service
mvn spring-boot:run
```

Uses H2 in-memory database — no setup required. Available at `http://localhost:8080/user-service`.

## Environment Variables

| Variable | Required | Default | Notes |
|----------|----------|---------|-------|
| `JWT_SECRET` | No | `eventmaster-shared-dev-secret-key-change-in-prod` | Must match all other services |
| `ADMIN_USERNAME` | No | `admin` | Username that receives admin privileges |

## Testing

```bash
mvn test
# Run a single class
mvn test -Dtest=UserServiceTest
```

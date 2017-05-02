
/**
 * Checks if the arguments matches the current user.
 * 
 * @param user parameter to check against the current user
 * @returns <code>true</code> if argument matches the current user
 */
function isCurrentUser(user) {
	return users.areEqual(users.current(), user);
}
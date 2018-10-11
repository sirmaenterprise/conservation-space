
/**
 * Checks if the arguments matches the current user.
 * <br>If the argument is java.util.Collection then checks if the current
 * user is part of the input collection
 * 
 * @param user parameter to check against the current user
 * @returns <code>true</code> if argument matches the current user
 */
function isCurrentUser(user) {
	if (user instanceof java.util.Collection) {
		var usersArray = user.toArray();
        for (var index in usersArray) {
			if (users.areEqual(users.current(), usersArray[index])) {
				return true;
			}
        }
        return false;
    }
    return users.areEqual(users.current(), user);
}

/**
 * Checks if the current user is member of the given group.
 * <br>If the argument is java.util.Collection then checks if the current
 * user is member in any of the input collection groups
 * 
 * @param group to check about membership of the current user
 * @returns <code>true</code> if the current user is member of the given group(s) 
 */
function isCurrentUserMemberOf(group) {
	if (group instanceof java.util.Collection) {
		var groupsArray = group.toArray();
		for (var index in groupsArray) {
			if (users.isMemberOf(users.current(), groupsArray[index])) {
				return true;
			}
		}
		return false;
	}
	return users.isMemberOf(users.current(), group);
}

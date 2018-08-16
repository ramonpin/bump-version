# bump-version
This is a sample project using clojure to implement version bump in a git repository
for a project that uses gradle for building.

It receives as parameters the path to the git repo an the new version and:

  * Syncs develop branch with upstream remote
  * Creates a b-version branch from develop branch
  * Pumps version on build.gradle to that version-SNAPSHOT
  * Commits the change and push the branch to upstream remote



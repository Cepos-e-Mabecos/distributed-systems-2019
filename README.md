# distributed-systems-2019
This repository contains our practical project for the class of distributed systems

### Status

### Contributing
1. [Fork](https://help.github.com/articles/fork-a-repo/) the project, clone your fork,
   and configure the remotes:
   ```bash
   # Clone your fork of the repo into the current directory
   git clone https://github.com/<your-username>/distributed-systems-2019.git
   # Navigate to the newly cloned directory
   cd bootstrap
   # Assign the original repo to a remote called "upstream"
   git remote add upstream https://github.com/Cepos-e-Mabecos/distributed-systems-2019.git
   ```
2. If you cloned a while ago, get the latest changes from upstream:
   ```bash
   git checkout master
   git pull upstream master
   ```
3. Create a new topic branch (off the main project development branch) to
   contain your feature, change, or fix:
   ```bash
   git checkout -b <topic-branch-name>
   ```
4. Commit your changes in logical chunks. Please adhere to these [git commit
   message guidelines](https://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html)
   or your code is unlikely be merged into the main project. Use Git's
   [interactive rebase](https://help.github.com/articles/about-git-rebase/)
   feature to tidy up your commits before making them public.
5. Locally merge (or rebase) the upstream development branch into your topic branch:
   ```bash
   git pull [--rebase] upstream master
   ```
6. Push your topic branch up to your fork:
   ```bash
   git push origin <topic-branch-name>
   ```
7. [Open a Pull Request](https://help.github.com/articles/about-pull-requests/)
    with a clear title and description against the `master` branch.

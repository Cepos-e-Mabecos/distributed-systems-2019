# distributed-systems-2019
This repository contains our practical project for the class of distributed systems
## Table of Contents
- [Status](#status)
- [Contributing](#contributing)
- [License](#license)

## Status

## Contributing
#### How to start
1. [Fork](https://help.github.com/articles/fork-a-repo/) the project, clone your fork, and configure the remotes:
```bash
# Clone your fork of the repo into the current directory
git clone https://github.com/<your-username>/distributed-systems-2019.git
# Navigate to the newly cloned directory
cd distributed-systems-2019
# Assign the original repo to a remote called "upstream"
git remote add upstream https://github.com/Cepos-e-Mabecos/distributed-systems-2019.git
```
2. If you cloned a while ago, get the latest changes from upstream:
```bash
git checkout master
git pull upstream master
```
3. Create a new topic branch (off the main project development branch) to contain your feature, change, or fix:
```bash
git checkout -b <topic-branch-name>
```
5. Locally merge (or rebase) the upstream development branch into your topic branch:
```bash
git pull [--rebase] upstream master
```
6. Push your topic branch up to your fork:
```bash
git push origin <topic-branch-name>
```
7. [Open a Pull Request](https://help.github.com/articles/about-pull-requests/) with a clear title and description against the `master` branch.

#### Code guidelines
- Code should follow all [Google Style](https://google.github.io/styleguide/javaguide.html) guidelines.
- Code should be formatted according with [Java Google Style](https://github.com/google/styleguide).

## License
By contributing your code, you agree to license your contribution under the [MIT License](https://github.com/Cepos-e-Mabecos/distributed-systems-2019/blob/master/LICENSE).

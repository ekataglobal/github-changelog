# GitHub changelog

[![Continuous Integration status](https://api.travis-ci.org/whitepages/github-changelog.svg?branch=master)](http://travis-ci.org/whitepages/github-changelog)

Pull-request based conventional changelog generator for GitHub projects which follow the AngularJS [Commit Message Format].


## Releases and Dependency Information

* Releases are published to TODO_LINK

* Latest stable release is TODO_LINK

* All released versions TODO_LINK

[Leiningen] dependency information:

    [github-changelog "0.1.0-SNAPSHOT"]

[Maven] dependency information:

    <dependency>
      <groupId>github-changelog</groupId>
      <artifactId>github-changelog</artifactId>
      <version>0.1.0-SNAPSHOT</version>
    </dependency>



## Usage

### Command Line Interface

It should be started with a [edn] config file
with the following content:

```edn
{:user       "user"
 :repo       "repo"
 :token      "0123456789abcdef0123456789abcdef01234567"}
``` 

Then start with:

    $ java -jar github-changelog.jar config.edn

This will clone the user/repo repository from GitHub into the current directory
and will generate the changelog for it.

You could also pass in a configuration on stdin if you set the config filename to `-`.

Here is an example usage:

    $ java -jar github-changelog.jar - < config.edn

### Config options

| key           | description                                                      | required |
|---------------|------------------------------------------------------------------|----------|
| `:user`       | Username for the repo                                            | ✓        |
| `:repo`       | Repository name                                                  | ✓        |
| `:token`      | GitHub [access token] to reach the API                           | ✓        |
| `:github`     | GitHub URL                                                       | ✗        |
| `:github-api` | URL for [GitHub Enterprise API]                                  | ✗        |
| `:jira`       | JIRA URL if you're using that for issue tracking                 | ✗        |
| `:dir`        | The destination directory for the repo                           | ✗        |
| `:update?`    | Clones or fetches the repository before generating the changelog | ✗        |
| `:git-url`    | Git URL for cloning if automatic generation does not suit you    | ✗        |

A more complete config example:

```edn
{:user       "user"
 :repo       "repo"
 :token      "0123456789abcdef0123456789abcdef01234567"
 :github     "https://github.example.com/"
 :github-api "https://github.example.com/api/v3/"
 :jira       "https://jira.atlassian.com/"
 :dir        "/tmp/destination-dir/"
 :update?    false}
```


## Change Log

## v0.2.0
#### Features
* **cli:**
  * read config from stdin [#59](https://github.com/whitepages/github-changelog/pull/59)
  * add --since option [#54](https://github.com/whitepages/github-changelog/pull/54)
#### Style Changes
* avoid using :refer [#58](https://github.com/whitepages/github-changelog/pull/58)
#### Chores
* update dependencies [#57](https://github.com/whitepages/github-changelog/pull/57)
* update boot [#55](https://github.com/whitepages/github-changelog/pull/55)
#### Refactorings
* clean up tag filtering [#56](https://github.com/whitepages/github-changelog/pull/56)
## v0.1.0
#### Chores
* update to use semver [#53](https://github.com/whitepages/github-changelog/pull/53)
* remove old coveralls.sh [#49](https://github.com/whitepages/github-changelog/pull/49)
* fix linting issues [#46](https://github.com/whitepages/github-changelog/pull/46)
* optimize requires everywhere [#43](https://github.com/whitepages/github-changelog/pull/43)
* update dependencies [#38](https://github.com/whitepages/github-changelog/pull/38)
* move BOOT_EMIT_TARGET to boot.properties [#45](https://github.com/whitepages/github-changelog/pull/45)
* add tests for github/get-pulls and refactor a bit [#23](https://github.com/whitepages/github-changelog/pull/23)
* upgrade dependencies [#22](https://github.com/whitepages/github-changelog/pull/22)
* fix kibit issues [#21](https://github.com/whitepages/github-changelog/pull/21)
* shorten down the namespace [#20](https://github.com/whitepages/github-changelog/pull/20)
* **boot:** add properties file to use 1.8.0 Clojure and update Travis config [#34](https://github.com/whitepages/github-changelog/pull/34)
* **ci:** adjust travis config for boot [#32](https://github.com/whitepages/github-changelog/pull/32)
#### Features
* make tag prefix configurable [#52](https://github.com/whitepages/github-changelog/pull/52)
* exclude reverted pull requests [#51](https://github.com/whitepages/github-changelog/pull/51)
* switch from tentacles to our own implementation [#13](https://github.com/whitepages/github-changelog/pull/13)
* **cli:**
  * add option to display changelog for only the last n tags [#47](https://github.com/whitepages/github-changelog/pull/47)
  * change from CLI parameters to config [#36](https://github.com/whitepages/github-changelog/pull/36)
* **config:**
  * add new config options [#39](https://github.com/whitepages/github-changelog/pull/39)
  * Use Immuconf to load configuration and move user and repo into Config schema [#12](https://github.com/whitepages/github-changelog/pull/12)
* **deps:** move from leiningen to boot, remove dev folder, remove schema [#30](https://github.com/whitepages/github-changelog/pull/30)
* **schemas:** add sha1 generator and use generators [#25](https://github.com/whitepages/github-changelog/pull/25)
* **dependencies:** add Bundler dependencies parser [#17](https://github.com/whitepages/github-changelog/pull/17)
* **markdown format:** add issue link and type translation [#10](https://github.com/whitepages/github-changelog/pull/10), closes [#9](https://github.com/whitepages/github-changelog/issues/9)
* **formatters:** translate changeset types [#9](https://github.com/whitepages/github-changelog/pull/9)
#### Documentations
* clarify what update? does [#50](https://github.com/whitepages/github-changelog/pull/50)
* **readme:** add short description and a link to the message format [#16](https://github.com/whitepages/github-changelog/pull/16)
#### Bug Fixes
* scope grouping (multi-level lists) [#29](https://github.com/whitepages/github-changelog/pull/29)
* sort the fetched keys by the version [#4](https://github.com/whitepages/github-changelog/pull/4)
* **github:** fetch pages only if there are more than one [#15](https://github.com/whitepages/github-changelog/pull/15)
* **boot:** fix uberjar generation [#35](https://github.com/whitepages/github-changelog/pull/35)
* **formatters/markdown:** increase highlight level [#48](https://github.com/whitepages/github-changelog/pull/48)
* **bundler:**
  * move sequence realization to with-open [#19](https://github.com/whitepages/github-changelog/pull/19)
  * fix large file reading [#18](https://github.com/whitepages/github-changelog/pull/18)
* **formatters:** fix scopeless formatting [#44](https://github.com/whitepages/github-changelog/pull/44)
* **cli:** properly working CLI [#14](https://github.com/whitepages/github-changelog/pull/14)
* **markdown:** fix formatting [#37](https://github.com/whitepages/github-changelog/pull/37)
* **issues:** remove trailing slash for JIRA URL [#42](https://github.com/whitepages/github-changelog/pull/42)
#### Style Changes
* fix indenting [#33](https://github.com/whitepages/github-changelog/pull/33)
#### Tests
* Add tests on invalid version tags [#31](https://github.com/whitepages/github-changelog/pull/31)
* fix tests and add coverage [#27](https://github.com/whitepages/github-changelog/pull/27)
* **bundler:** add tests [#28](https://github.com/whitepages/github-changelog/pull/28)
#### Refactorings
* cleanups [#26](https://github.com/whitepages/github-changelog/pull/26)
* github/gen-pages [#24](https://github.com/whitepages/github-changelog/pull/24)
* use the fact that (str nil) => "" [#7](https://github.com/whitepages/github-changelog/pull/7)
* remove unnecessary usage of threading macros, use shorthand Java interop [#6](https://github.com/whitepages/github-changelog/pull/6)
* use threading macro to make code read more naturally [#5](https://github.com/whitepages/github-changelog/pull/5)
* use subs(tring) instead of seq operations and inline if [#3](https://github.com/whitepages/github-changelog/pull/3)
* only do network IO when accessed [#2](https://github.com/whitepages/github-changelog/pull/2)
* extend repo fn with 2-arity version [#1](https://github.com/whitepages/github-changelog/pull/1)
* **markdown:**
  * simplify ul [#11](https://github.com/whitepages/github-changelog/pull/11)
  * convenience wrappers around header and partial fn application [#8](https://github.com/whitepages/github-changelog/pull/8)


## Copyright and License

Copyright © 2015-2016 Whitepages Inc.

Distributed under [MIT license](http://choosealicense.com/licenses/mit/).

[Leiningen]: http://leiningen.org/
[Maven]: http://maven.apache.org/
[Commit Message Format]: https://github.com/angular/angular.js/blob/master/DEVELOPERS.md#commit-message-format
[edn]: https://github.com/edn-format/edn
[access token]: https://help.github.com/articles/creating-an-access-token-for-command-line-use/
[GitHub Enterprise API]: https://developer.github.com/v3/enterprise/

# GitHub changelog

[![Continuous Integration status](https://api.travis-ci.org/whitepages/github-changelog.svg?branch=master)](http://travis-ci.org/whitepages/github-changelog)
[![Docker Build Status](https://img.shields.io/docker/build/jrottenberg/ffmpeg.svg)](https://hub.docker.com/r/whitepages/github-changelog/)
[![Docker pulls](https://img.shields.io/docker/pulls/whitepages/github-changelog.svg)](https://hub.docker.com/r/whitepages/github-changelog/)
[![GitHub license](https://img.shields.io/github/license/whitepages/github-changelog.svg)](https://github.com/whitepages/github-changelog/blob/master/LICENSE)

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

* Version 0.1.0-SNAPSHOT


## Copyright and License

Copyright © 2015-2016 Whitepages Inc.

Distributed under [MIT license](http://choosealicense.com/licenses/mit/).

[Leiningen]: http://leiningen.org/
[Maven]: http://maven.apache.org/
[Commit Message Format]: https://github.com/angular/angular.js/blob/master/DEVELOPERS.md#commit-message-format
[edn]: https://github.com/edn-format/edn
[access token]: https://help.github.com/articles/creating-an-access-token-for-command-line-use/
[GitHub Enterprise API]: https://developer.github.com/v3/enterprise/

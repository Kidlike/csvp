# CSVP

A csv -> csv processor with user defined JS to transform each row.

Written with minimal effort, with the help and limitations of https://quarkus.io/ :)

### Installing this
- Grab the binary or jar file from [/releases](https://github.com/Kidlike/csvp/releases/)
- or build yourself (see bellow)

### Running this

- **Needs** the `SMALLRYE_CONFIG_LOCATIONS` env-var to be set, and pointing to a `yaml` config file.
  - _https://github.com/quarkusio/quarkus/issues/1218_
- Other i/o options are provided from the command line:

```
Usage: csvp [-hV] [-i=file] [-o=file]
  -h, --help          Show this help message and exit.
  -i, --input=file    Read from <file> instead of stdin.
                      Omitting this option will run in interactive mode.
  -o, --output=file   Write output to <file> instead of stdout.
  -V, --version       Print version information and exit.
```

#### Example: reverse column order
```
$ cat sample.yaml
input:
    delimiter: >-
        ,
output:
    delimiter: >-
        ,
    row-transform: >-
        return [
            row[1],
            row[0]
        ];

$ cat sample.csv
1,a
2,b
3,c

$ SMALLRYE_CONFIG_LOCATIONS=sample.yaml csvp-1.0 -i sample.csv
a,1
b,2
c,3
```

#### Full YAML config file

```yaml
input: # Configs for the input CSV file
    delimiter: ;
    has-header: false # Whether the input contains a header or not (defaults to true)
output: # Configs for the CSV output
    delimiter: ;
    headers: # Omitting this will not produce a header line
        - Date
        - Category
        - Value
    row-transform: >- # This is the main entry point, in JS. Basically: function map(row) { ... return []; }
        /* feel free to declare functions too */
        var isEmpty = function(v) {
            return v == '' || v == null || v == {} || v == [];
        };

        /* grab some values from your very large and complicated input CSV */
        var bookingText = row[4];
        var bookingDate = row[12];

        /* convert 1'500.00 -> 1500 */
        var debit = row[10].replace(/[^\d]/g,"")/100;

        /* skip rows by returning null */
        if (isEmpty(bookingDate)) return null;
        if (debit <= 0) return null;

        var bookingDateTokens = bookingDate.split('.');
        var date = new Date(Date.parse(bookingDateTokens[1] + "." +bookingDateTokens[0] + "." +bookingDateTokens[2])).toISOString();

        var category = (/(steam|netflix|spotify)/i.test(bookingText)) ? 'Entertainment'
            : (/(pizza|taco|burger)/i.test(bookingText)) ? 'Food and Drinks'
            : (/(lidl|ikea)/i.test(bookingText)) ? 'Home'
            : 'Misc';

        return [
            date,
            caregory,
            debit * -1
        ];
```

### Building the application

#### Prerequisites

To build this project you will need:

- Maven 3.6.3
- JDK 11 + GraalVM

The easiest way is to use https://sdkman.io/
- `sdk install java 20.3.0.r11-grl`
- `sdk install maven 3.6.3`

```
$ java --version
openjdk 11.0.9 2020-10-20
OpenJDK Runtime Environment GraalVM CE 20.3.0 (build 11.0.9+10-jvmci-20.3-b06)
OpenJDK 64-Bit Server VM GraalVM CE 20.3.0 (build 11.0.9+10-jvmci-20.3-b06, mixed mode, sharing)

$ mvn --version
Apache Maven 3.6.3 (cecedd343002696d0abb50b32b541b8a6ba2883f)
Maven home: /home/kidlike/.sdkman/candidates/maven/current
Java version: 11.0.9, vendor: GraalVM Community, runtime: /home/kidlike/.sdkman/candidates/java/20.3.0.r11-grl
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "5.9.16-200.fc33.x86_64", arch: "amd64", family: "unix"
```

#### Compile to jar

- `mvn clean install -DskipTests` produces `target/csvp-1.0-SNAPSHOT-runner.jar`

#### Compile to native binary

- `mvn clean install -DskipTests -Pnative` produces `target/csvp-1.0-SNAPSHOT-runner`

### Details of native binary

There should be no system dependencies for the native binary:
```
$ ldd target/csvp-1.0-SNAPSHOT-runner
        linux-vdso.so.1 (0x00007ffe82837000)
        libpthread.so.0 => /lib64/libpthread.so.0 (0x00007fdda7ae5000)
        libdl.so.2 => /lib64/libdl.so.2 (0x00007fdda7ade000)
        libz.so.1 => /lib64/libz.so.1 (0x00007fdda7ac4000)
        librt.so.1 => /lib64/librt.so.1 (0x00007fdda7ab9000)
        libc.so.6 => /lib64/libc.so.6 (0x00007fdda78ee000)
        /lib64/ld-linux-x86-64.so.2 (0x00007fdda7b3c000)
```

#### Performance of native binary

This is just a preliminary test to get an order-of-magnitude idea of performance.

Basic hardware specs:
- i7-7820HQ
- nvme disk
- Fedora 33 - kernel 5.9.16

I used a real personal example with some complexity:
- 68 lines of javascript with 18 regular expressions, 14 variables, and 2 functions
- input CSV of 21 columns in various size samples: 10k, 50k, and 100k rows (34MB)

=> performance outcomes were between **20000 and 30000 rows / second**

_I think that smaller files have worse performance because hardware fluctuations have a more significant impact (CPU context switching, etc)_


### Motivation

- A real personal need for a flexible but fast* CSV -> CSV processor.
- An opportunity to play around with new Java kids, Quarkus and GraalVM.

_\*fast: see my own personal benchmark bellow._

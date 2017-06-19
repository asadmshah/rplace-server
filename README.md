# r/place Server

An attempt at building the reddit r/place April 1st project.

## Usage

Use `docker-compose` to get it up and running quickly. Not much effort was put
put into making it docker-ready so the recommended course of action to get it up and
running quickly is the following:

1. `docker-compose up zookeeper`
2. `docker-compose up kafka1 kafka2 kafka3`
3. `docker-compose up redis`
4. `docker-compose up drawcommitter`
5. `docker-compose up web1 web2 web3`
6. `docker-compose up haproxy`

You should be able to websocket at this point.

If you'd like to get some random bot-action going on you can start the drawbots
container:

7. `docker-compose up drawbots`

Use the `scale` argument and modify the `THREAD_COUNT` and `HIT_INTERVAL_MILLIS` 
to your liking.

Have an Android device or emulator? Use [this client](https://www.github.com/asadmshah/rplace-android)
to use it.

# License

    MIT License

    Copyright (c) 2017 Asad Shah

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.


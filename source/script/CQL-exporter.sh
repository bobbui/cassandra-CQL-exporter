#!/bin/sh

# Copyright (c) 2016, Bob. All rights reserved.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
java $JAVA_OPTS -jar cql-exporter.jar "$@"
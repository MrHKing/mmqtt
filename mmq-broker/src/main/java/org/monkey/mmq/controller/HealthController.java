/*
 * Copyright 2021-2021 Monkey Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.monkey.mmq.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("consoleHealth")
public class HealthController {
    /**
     * Whether the MMQ is in broken states or not, and cannot recover except by being restarted.
     *
     * @return HTTP code equal to 200 indicates that MMQ is in right states. HTTP code equal to 500 indicates that
     * MMQ is in broken states.
     */
    @GetMapping("/liveness")
    public ResponseEntity<String> liveness() {
        return ResponseEntity.ok().body("OK");
    }
}

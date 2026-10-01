/*
 * Copyright 2026 CodeMatters, Lda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

import io.spine.dependency.local.TestLib
import io.spine.gradle.publish.IncrementGuard

// Apply plugins to make type-safe extension accessors available in this script file.
plugins {
    module
    `project-report`
}
apply<IncrementGuard>()

dependencies {
    // Contains Spine framework annotations (e.g., @Internal) used for marking internal APIs.
    // This module has no external runtime dependencies.
    
    // Test dependencies
    testImplementation(TestLib.lib)
}

dokka {
    moduleName.set("Annotations")
}

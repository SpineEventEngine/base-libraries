/*
 * Copyright 2026, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.query;

import com.google.protobuf.Message;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates the types that define the columns of the Protobuf messages stored as records
 * in a storage.
 *
 * <p>We recommend following some naming patterns and approaches when designing such types.
 *
 * <p>Let's review an example:
 *
 * <pre>
 *     import static io.spine.query.RecordColumn.create;
 *
 *     // ...
 *
 * {@literal    @RecordColumns(ofType = ProjectView.class)                                     }
 *     public static class ProjectViewColumns {
 *
 * {@literal       public static final RecordColumn<ProjectView, ProjectName> project_name =   }
 *               create("project_view", ProjectName.class, ProjectView::getProjectName);
 *
 * {@literal       public static final RecordColumn<ProjectView, UserName> assignee =          }
 * {@literal        create("assignee", UserName.class, (p) -> p.getAssignee().getName());      }
 *     }
 * </pre>
 *
 * <p>The columns are declared as {@code public static final} fields. There are two reasons
 * for that.
 *
 * <ol>
 *     <li>It allows referring to columns via a {@code static import}, as it's nice to shorten
 *     the expression in which a column declaration is used.
 *
 *     <li>We want to preserve the type of the column value (which is determined by the second
 *     generic parameter) in order to put bounds to the compared values in scope of a query.
 * </ol>
 *
 * <p>Here is how it plays together with a {@code RecordQuery}:
 *
 * <pre>
 *     import static com.acme.ProjectViewColumns.project_name;
 *
 *     // ...
 *
 *     ProjectName myProjectName = // ...
 *
 *     // Selects all `ProjectView`s by the project name.
 * {@literal    RecordQuery<ProjectId, ProjectView> query =                                   }
 *             RecordQuery.newBuilder(ProjectId.class, ProjectView.class)
 *                        .where(project_name).is(myProjectName)
 *                        .build();
 * </pre>
 *
 * <p>Note that {@code myProjectName} is checked to be of {@code ProjectName} type, at the time
 * of code compilation.
 *
 * <p>Also, note the snake_case notation used in the declaration of the {@code project_name} field.
 * As many field names contain at least two words, such an approach allows easily distinguishing
 * a field from a Java variable holding a value to which the field values are compared
 * ({@code myProjectName} in the example above).
 *
 * @see RecordColumn
 * @see RecordQuery
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface RecordColumns {

    /**
     * Returns the type of the message record, which columns are being described
     * by the annotated type.
     */
    Class<? extends Message> ofType();
}

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

/**
 * This package defines the query DSL for the stored Protobuf messages.
 *
 * <h3>Preface</h3>
 *
 * <p>A significant part of the Protobuf messages emitted in the Spine-powered applications are
 * written and read from a persistent storage at some point in their lifecycle. At the same time,
 * Spine apps follow the hexagonal architecture. Instead of writing a database-specific code,
 * developers deal with an abstraction over a database port. Under these circumstances,
 * a convenient language is required to describe the how the message data is saved and retrieved.
 *
 * <p>Proto messages may have complex structure that hardly maps to a flat structure of most
 * relational databases and even some NoSQL storages. Therefore, when a Protobuf message
 * is persisted, it is typically transformed to a byte array through the native Protobuf
 * serialization mechanism. The resulting record is stored along with its identifier.
 * It's worth mentioning that the Protobuf binary format makes the records compact in size and
 * resilient to any future changes made to the declaration of a stored {@code Message}. There
 * are more information available on the Protobuf serialization performance
 * <a href="https://dzone.com/articles/protobuf-performance-comparison-and-points-to-make">over
 * the web</a>.
 *
 * <p>While storing a {@code Message} as a BLOB simplifies writing, it makes searching and
 * filtering of the stored records difficult. Except for IDs, no attributes of persisted messages
 * are exposed to the search engine of a DBMS by default. Therefore, the framework allows defining
 * additional attributes of a message to enable querying. In terms of storage language,
 * each declared attribute is persisted as a "column". Please note that on this level of abstraction
 * a developer does not define the structure of an underlying database; it's still a responsibility
 * of a particular storage port.
 *
 * <p>This package provides an API on building the queries for the stored records, rather than
 * configuring the storage port. Other libraries of the framework, such as {@code spine-server} and
 * {@code spine-datastore} concentrate on setting up the underlying database and interacting with it.
 *
 * <h3>Record Queries</h3>
 *
 * <p>In scope of this document, a "record" means a Protobuf message persisted into a storage.
 * Each record has an identifier, a serialized {@code Message} content as BLOB, and potentially
 * declares one or more record columns. A column is typically a part of a stored message
 * (e.g. a field, or a value of a function of several fields) that is stored in the same DB object
 * or a table row.
 *
 * <p>The stored records may be queried in one of three ways:
 *
 * <ul>
 *     <li>by the value of the record identifiers;</li>
 *
 *     <li>by the values of the stored columns;</li>
 *
 *     <li>all records, without any criteria or restrictions.</li>
 * </ul>
 *
 * <p>Let's think of some example {@code Message}:
 *
 * <pre>
 * // Describes a car manufacturer as a company.
 * message Manufacturer {
 *
 *     ManufacturerId id = 1 [(required) = true, (validate) = true];
 *
 *     // When the company was founded.
 *     google.protobuf.Timestamp when_founded = 2 [(required) = true];
 *
 *     // How many employees work in the company.
 *     int32 number_of_employees = 3 [(min).value = "1"];
 *
 *     // The type of the company as a legal entity.
 *     LegalEntityType type = 4;
 *
 *     // If traded, which stock symbols the company uses.
 *     repeated TradeStockSymbol stock_symbol = 5;
 * }
 * </pre>
 *
 * <p>A developer may want to choose a couple of values to be stored as distinct columns:
 *
 * <pre>
 *  public static final class ManufacturerColumns {
 *
 *   {@literal public static final RecordColumn<Manufacturer, Timestamp> whenFounded = }
 *   {@literal         new RecordColumn<>("when_founded", }
 *                               Timestamp.class,
 *                               Manufacturer::getWhenFounded);
 *
 *   {@literal public static final RecordColumn<Manufacturer, Boolean> isTraded = }
 *   {@literal         new RecordColumn<>("is_traded", }
 *                               Boolean.class,
 *   {@literal                            (r) -> !r.getStockSymbolList()          }
 *                                        .isEmpty());
 *
 *    private ManufacturerColumns() {
 *    }
 * }
 * </pre>
 *
 * <p>In this case, {@code Manufacturer.when_founded} field of the message is stored as-is,
 * while the {@code is_traded} column is declared to contain the value computed from
 * the passed {@code r}, being the instance of {@code Manufacturer} message.
 *
 * <p>Once the columns are declared, they may be used in a query. The framework provides
 * a {@link io.spine.query.RecordQueryBuilder RecordQueryBuilder} for this purpose:
 *
 * <pre>
 *    {@literal RecordQuery<ManufacturerId, Manufacturer> query = }
 *        RecordQuery.newBuilder(ManufacturerId.class, Manufacturer.class)
 *                   .where(whenFounded).isLessOrEqualTo(THURSDAY)
 *                   .where(isTraded).is(true)
 *                   .build();
 * </pre>
 *
 * <p>Where {@code whenFounded} and {@code isTraded} are the statically imported definitions
 * from the {@code ManufacturerColumns} class defined above. The types of the values passed to
 * the {@link io.spine.query.QueryCriterion#isLessOrEqualTo(java.lang.Object) isLessOrEqualTo(..)}
 * and {@linkplain io.spine.query.QueryCriterion#is(java.lang.Object) is(..)} methods strictly
 * correspond to the types of the declared columns. That is, {@code whenFounded} column would
 * only accept {@code Timestamp} values for comparison, and {@code isTraded} only
 * accepts {@code Boolean}s.
 *
 * <p>It is also possible to build a query with a disjunction (OR) operator:
 *
 * <pre>
 *     {@literal RecordQuery<ManufacturerId, Manufacturer> query = }
 *                 manufacturerBuilder()
 *     {@literal                .either((b) -> b.where(whenFounded).isLessThan(firstJanuary1990), }
 *     {@literal                        (b) -> b.where(isTraded).is(true))                        }
 *                             .build();
 * </pre>
 *
 * <p>Where {@code b} is the instance of the same {@code RecordQueryBuilder}, for proper chaining.
 * This query selects all the records that have either {@code whenFounded} value less than
 * January, 1, 1990, or the {@code isTraded} value equal to {@code true}.
 *
 * <p>Please note that {@code ManufacturerId} is used as a type of the record identifier when
 * creating a builder. It is the first field in this message — in the order of reading
 * (top to bottom), which matches its field number (1) in this case. Its values may
 * also be used in a query builder:
 *
 * <pre>
 *    {@literal RecordQuery<ManufacturerId, Manufacturer> query = }
 *        RecordQuery.newBuilder(ManufacturerId.class, Manufacturer.class)
 *                   .id().is(someManufacturer)
 *                   .build();
 * </pre>
 *
 * <p>Where {@code someManufacturer} must a value of {@code ManufacturerId} type. In addition
 * to {@link io.spine.query.IdCriterion#is(java.lang.Object) is(value)}, the framework also provides
 * {@link io.spine.query.IdCriterion#in(java.lang.Object[]) in(...values)} and
 * {@link io.spine.query.IdCriterion#in(java.lang.Iterable) in(Iterable values)}
 * overloads. They select the records by several ID values.
 *
 * <h4>Sorting and limit</h4>
 *
 * <p>The query builder API also allows specifying the sorting order and the record limit
 * for the queries:
 *
 * <pre>
 *     {@literal RecordQuery<ManufacturerId, Manufacturer> query = }
 *     {@literal    RecordQuery.newBuilder(ManufacturerId.class, Manufacturer.class) }
 *                    .sortAscendingBy(whenFounded)
 *                    .sortDescendingBy(isTraded)
 *                    .limit(10)
 *                    .build();
 * </pre>
 *
 * <p>This query selects all records of {@code Manufacturer} type, but tells to sort them
 * by the values of {@code whenFounded} column ascending, then by {@code isTraded} column values
 * descending. Finally, the query result is limited to the top ten records.
 *
 * <p>Please note, that {@linkplain io.spine.query.RecordQueryBuilder#limit(int) limit(..)}
 * method may only be used in queries that sort their results. Otherwise, a runtime exception
 * is thrown upon building the query.
 *
 * <h4>Field masks</h4>
 *
 * <p>Sometimes end-users are interested in obtaining only a part of the fields of stored messages.
 * In this case, they may specify a {@link com.google.protobuf.FieldMask FieldMask} to be applied
 * to each of the resulting records. The functionality of the field masks is supported fully,
 * as per the <a href="https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/FieldMask">original contract</a>.
 *
 * <p>Let's see it in action:
 *
 * <pre>
 *      FieldMask mask = fieldMaskWith(isTraded);   // Only keep the `is_traded` field in results.
 *     {@literal RecordQuery<ManufacturerId, Manufacturer> query = }
 *     {@literal    RecordQuery.newBuilder(ManufacturerId.class, Manufacturer.class) }
 *                    .withMask(mask)
 *                    .build();
 * </pre>
 *
 * <p>The query above selects all records of {@code Manufacturer} type, but applies the field
 * mask to each of the resulting records. Depending on the implementation of an underlying storage,
 * the field mask value may be used to optimize the call to the native storage, and fetch
 * only the required values, for instance, if they are stored separately as columns.
 *
 * <h3>Entity Queries</h3>
 *
 * <p>Among all the stored Protobuf messages, there is a special case. If a message is declared as
 * an Entity state, it triggers an additional processing on a build-time. The framework
 * automatically extends the Java types generated for Entity states with more Entity-specific API.
 *
 * <p>Let's consider a message:
 *
 * <pre>
 * message ProjectView {
 *    option (entity).kind = PROJECTION;
 *
 *    // The identifier of the project.
 *    ProjectId project = 1;
 *
 *    ProjectName project_name = 2 [(column) = true];
 *
 *    Project.Status status = 3 [(column) = true];
 *
 *    UserView assignee = 4;
 *
 *    int32 days_since_started = 5 [(column) = true];
 *
 *    bool was_reassigned = 6 [(column) = true];
 *
 *    repeated string tag = 7;
 * }
 * </pre>
 *
 * <p>The {@code ProjectView} message is declared as a state of a future Projection. As it is
 * a part of a Ubiquitous Language, its Protobuf definition serves as a contract for everyone
 * wishing to interact with it. In particular, this message marks some of its fields
 * with the {@code (column)} option. It tells everyone, that the instances of this Projection
 * will be available for querying by the values of these fields.
 *
 * <p>In terms of the code generation, the framework automatically appends the corresponding
 * Java type with the nested {@code Query} and {@code QueryBuilder} types. They are composed
 * at build-time and contain the definitions of the columns for the respective storage record,
 * and the query DSL.
 *
 * <p>Notice the difference with the {@link io.spine.query.RecordQueryBuilder RecordQueryBuilder}
 * API, as the developer no longer needs to declare any types by hand.
 *
 * <p>Here is an example:
 *
 * <pre>
 *     ProjectView.Query query = ProjectView.query()
 *        .status().isNot(Status.CREATED)
 *        .daysSinceStarted().isGreaterThan(5)
 *        .build();
 * </pre>
 *
 * <p>It produces a query for all the {@code ProjectView} records that have a status other than
 * {@code CREATED} and that were started more than five days ago.
 *
 * <p>Disjunction (or OR statements) are also supported:
 *
 * <pre>
 *    {@literal Either<ProjectView.QueryBuilder> startedMoreThanMonthAgo =   }
 *    {@literal     project -> project.daysSinceStarted().isGreaterThan(30); }
 *    {@literal Either<ProjectView.QueryBuilder> isDone =                    }
 *    {@literal     project -> project.status().is(Status.DONE);             }
 *
 * ProjectView.Query query =
 *        ProjectView.query()
 *                   .either(startedMoreThanMonthAgo, isDone)
 *                   .build();
 * </pre>
 *
 * <p>This piece produces a query targeting the projects that are either done or started more
 * than thirty days ago.
 *
 * <p>The first field of the {@code Message} is treated as
 * an {@linkplain io.spine.base.Identifier identifier}.
 * The Spine framework treats the field declared first in the order of reading
 * (top to bottom in the message definition), not the one with the least index value.
 * For example:
 *
 * <pre>
 * message UserView {
 *    option (entity).kind = PROJECTION;
 *
 *    UserName name = 2;  // This is the ID field (first in reading order)
 *    UserId id = 1;       // Not the ID field (second in reading order)
 * }
 * </pre>
 *
 * <p>In this example, {@code name} is treated as the identifier because it appears first in
 * the declaration, even though its field number is 2.
 *
 * <p>The name of the ID field is preserved and is exposed for querying:
 *
 * <pre>
 *     ProjectView.Query query = ProjectView.query()
 *                                          .project().is(expectedId)
 *                                          .build();
 * </pre>
 *
 * <h4>Custom Columns</h4>
 *
 * <p>In addition to the columns declared in the Entity state, the query API allows declaring
 * the custom columns. They are intended to handle the cases in which some computed on-the-fly data
 * should be stored along with the record. It may the time of entity creation, the role of the
 * user created the record, whether the entity is deleted or archived etc. That is, something
 * that isn't included into the definition of the original {@code Message} type of the record.
 *
 * <pre>
 * // The column presumably defined as a custom column for querying.
 * class ArchivedColumn extends{@literal CustomColumn<EntityWithLifecycle, Boolean>} {
 *
 *   {@literal @Override }
 *    public ColumnName name() {
 *        return ColumnName.of("archived");
 *    }
 *
 *   {@literal @Override }
 *   {@literal public Class<Boolean>} type() {
 *        return Boolean.class;
 *    }
 *
 *   {@literal @Override }
 *    public Boolean valueIn(EntityWithLifecycle source) {
 *        return source.isArchived();
 *    }
 * }
 * </pre>
 *
 * <p>Where {@code EntityWithLifecycle} is a sample interface serving as a source of the lifecycle
 * values for the Entity types.
 *
 * <pre>
 * // Custom columns that define Entity lifecycle.
 * public enum Lifecycle {
 *
 *    ARCHIVED(new ArchivedColumn()),
 *
 *    DELETED(new DeletedColumn());
 *
 *   {@literal private final CustomColumn<?, Boolean>} column;
 *
 *   {@literal Lifecycle(CustomColumn<?, Boolean>} column) {
 *        this.column = column;
 *    }
 *
 *    // Returns the column declaration.
 *   {@literal public CustomColumn<?, Boolean>} column() {
 *        return column;
 *    }
 * }
 * </pre>
 *
 * <p>Such an approach allows defining the singleton instances of the columns and using
 * them in querying:
 *
 * <pre>
 *     ProjectView.Query queryForDeleted =
 *        ProjectView.query()
 *                   .where(DELETED.column(), true)
 *                   .build();
 * </pre>
 *
 * <h4>Sorting and limit</h4>
 *
 * <p>Similar to the plain record query API, it is possible to set the same sorting parameters
 * when querying the Entity state records:
 *
 * <pre>
 *     ProjectView.Query query = ProjectView.query()
 *        .sortAscendingBy(daysSinceStarted())
 *        .sortAscendingBy(projectName())
 *        .sortDescendingBy(wasReassigned())
 *        .limit(10)
 *        .build();
 * </pre>
 *
 * <p>Here {@code daysSinceStarted()}, {@code projectName()} and {@code wasReassigned()} are three
 * static methods generated in {@code ProjectView.Column} class. Each of them returns
 * the declaration of the Entity column corresponding to the Message field with
 * the {@code (column)} option.
 *
 * <h4>Field masks</h4>
 *
 * <p>It is also possible to set the mask for each resulting Protobuf message:
 *
 * <pre>
 *     FieldMask mask = fieldMaskWith(status());
 *     ProjectView.Query query = ProjectView.query()
 *                                          .withMask(mask)
 *                                          .build();
 * </pre>
 *
 * <h4>Entity states with no columns</h4>
 *
 * <p>If a Proto message declares an Entity state with no columns, the framework still requires
 * at least one field declared. As described above, it is treated as an Entity identifier.
 * Therefore, both {@code QueryBuilder} and {@code Query} are generated for this case, allowing
 * to query the Entity state records by their identifiers.
 *
 * <h3>Further reading</h3>
 *
 * <p>To see the Query API in action, please feel free to navigate to the corresponding test
 * classes residing in the same package of the {@code test} source root.
 */
@CheckReturnValue
@NullMarked
package io.spine.query;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.NullMarked;

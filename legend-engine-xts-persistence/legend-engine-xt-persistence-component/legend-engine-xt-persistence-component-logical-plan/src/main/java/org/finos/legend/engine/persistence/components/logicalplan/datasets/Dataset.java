// Copyright 2022 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.engine.persistence.components.logicalplan.datasets;

import org.finos.legend.engine.persistence.components.logicalplan.LogicalPlanNode;

import java.util.Optional;

public interface Dataset extends LogicalPlanNode
{
    default Optional<DatasetAdditionalProperties> datasetAdditionalProperties()
    {
        throw new UnsupportedOperationException();
    }

    default SchemaDefinition schema()
    {
        throw new UnsupportedOperationException();
    }

    default SchemaReference schemaReference()
    {
        throw new UnsupportedOperationException();
    }

    default DatasetReference datasetReference()
    {
        throw new UnsupportedOperationException();
    }

    default Dataset withSchema(SchemaDefinition schema)
    {
        throw new UnsupportedOperationException();
    }
}

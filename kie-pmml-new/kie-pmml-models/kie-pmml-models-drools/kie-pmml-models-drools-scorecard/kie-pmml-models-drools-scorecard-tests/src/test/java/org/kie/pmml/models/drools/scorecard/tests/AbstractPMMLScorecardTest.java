/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.pmml.models.drools.scorecard.tests;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.dmg.pmml.PMML;
import org.dmg.pmml.scorecard.Scorecard;
import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.kie.api.KieBase;
import org.kie.api.builder.ReleaseId;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.internal.utils.KieHelper;
import org.kie.pmml.commons.model.KiePMMLModel;
import org.kie.pmml.compiler.testutils.TestUtils;
import org.kie.pmml.evaluator.core.executor.PMMLModelExecutor;
import org.kie.pmml.evaluator.core.utils.PMMLRequestDataBuilder;
import org.kie.pmml.models.drools.scorecard.compiler.executor.ScorecardModelImplementationProvider;
import org.kie.pmml.models.drools.scorecard.evaluator.PMMLScorecardModelEvaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractPMMLScorecardTest {

    protected static final ScorecardModelImplementationProvider PROVIDER = new ScorecardModelImplementationProvider();
    protected static final PMMLModelExecutor EXECUTOR = new PMMLScorecardModelEvaluator();
    private static final ReleaseId RELEASE_ID = new ReleaseIdImpl("org.drools", "kie-pmml-models-testing", "1.0");
    protected static KieBase kieBase;
    protected static PMMLRequestData getPMMLRequestData(String modelName, Map<String, Object> parameters) {
        String correlationId = "CORRELATION_ID";
        PMMLRequestDataBuilder pmmlRequestDataBuilder = new PMMLRequestDataBuilder(correlationId, modelName);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            Object pValue = entry.getValue();
            Class class1 = pValue.getClass();
            pmmlRequestDataBuilder.addParameter(entry.getKey(), pValue, class1);
        }
        return pmmlRequestDataBuilder.build();
    }

    protected static KiePMMLModel loadPMMLModel(final String resourcePath) {
        final PMML pmml;

        try {
            pmml = TestUtils.loadFromFile(resourcePath);
        } catch (Exception e) {
            throw new RuntimeException("Error loading PMML", e);
        }

        Assertions.assertThat(pmml).isNotNull();
        assertEquals(1, pmml.getModels().size());
        assertTrue(pmml.getModels().get(0) instanceof Scorecard);
        KnowledgeBuilderImpl knowledgeBuilder = new KnowledgeBuilderImpl();
        final KiePMMLModel pmmlModel = PROVIDER.getKiePMMLModel(pmml.getDataDictionary(),
                                                                (Scorecard) pmml.getModels().get(0), knowledgeBuilder);
        Assertions.assertThat(pmmlModel).isNotNull();
        kieBase = new KieHelper()
                .addContent(knowledgeBuilder.getPackageDescrs(pmmlModel.getName().toLowerCase()).get(0))
                .setReleaseId(RELEASE_ID)
                .build();
        Assertions.assertThat(kieBase).isNotNull();
        return pmmlModel;
    }
}

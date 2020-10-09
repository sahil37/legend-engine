// Copyright 2020 Goldman Sachs
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

package org.finos.legend.engine.language.pure.grammar.from.domain;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.finos.legend.engine.language.pure.grammar.from.DEPRECATED_SectionGrammarParser;
import org.finos.legend.engine.language.pure.grammar.from.ParseTreeWalkerSourceInformation;
import org.finos.legend.engine.language.pure.grammar.from.ParserErrorListener;
import org.finos.legend.engine.language.pure.grammar.from.PureGrammarParserContext;
import org.finos.legend.engine.language.pure.grammar.from.SourceCodeParserInfo;
import org.finos.legend.engine.language.pure.grammar.from.antlr4.domain.DomainLexerGrammar;
import org.finos.legend.engine.language.pure.grammar.from.antlr4.domain.DomainParserGrammar;
import org.finos.legend.engine.protocol.pure.v1.model.SourceInformation;
import org.finos.legend.engine.protocol.pure.v1.model.context.PureModelContextData;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.domain.Domain;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.section.ImportAwareCodeSection;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.section.Section;
import org.finos.legend.engine.protocol.pure.v1.model.valueSpecification.ValueSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.valueSpecification.raw.Lambda;

import java.util.ArrayList;
import java.util.List;

public class DomainParser implements DEPRECATED_SectionGrammarParser
{
    public static final String name = "Pure";

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public SourceCodeParserInfo getParserInfo(String code, SourceInformation sourceInformation, ParseTreeWalkerSourceInformation walkerSourceInformation)
    {
        return this.getParserInfo(code, sourceInformation, walkerSourceInformation, true);
    }

    private SourceCodeParserInfo getParserInfo(String code, SourceInformation sectionSourceInformation, ParseTreeWalkerSourceInformation walkerSourceInformation, boolean includeRootContext)
    {
        CharStream input = CharStreams.fromString(code);
        ParserErrorListener errorListener = new ParserErrorListener(walkerSourceInformation);
        DomainLexerGrammar lexer = new DomainLexerGrammar(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        DomainParserGrammar parser = new DomainParserGrammar(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        return new SourceCodeParserInfo(code, input, sectionSourceInformation, walkerSourceInformation, lexer, parser, includeRootContext ? parser.definition() : null);
    }

    @Override
    public Section parse(SourceCodeParserInfo sectionParserInfo, PureModelContextData pureModelContextData, PureGrammarParserContext parserContext)
    {
        ImportAwareCodeSection section = new ImportAwareCodeSection();
        section.parserName = this.getName();
        section.sourceInformation = sectionParserInfo.sourceInformation;
        DomainParseTreeWalker walker = new DomainParseTreeWalker(sectionParserInfo.walkerSourceInformation, parserContext, section);
        Domain domain = walker.VisitDefinition((DomainParserGrammar.DefinitionContext) sectionParserInfo.rootContext);
        if (pureModelContextData.domain == null)
        {
            pureModelContextData.domain = domain;
        }
        else
        {
            pureModelContextData.domain.classes.addAll(domain.classes);
            pureModelContextData.domain.associations.addAll(domain.associations);
            pureModelContextData.domain.enums.addAll(domain.enums);
            pureModelContextData.domain.profiles.addAll(domain.profiles);
            pureModelContextData.domain.functions.addAll(domain.functions);
            pureModelContextData.domain.measures.addAll(domain.measures);
        }
        return section;
    }

    public Lambda parseLambda(String code, String lambdaId)
    {
        ParseTreeWalkerSourceInformation lambdaWalkerSourceInformation = new ParseTreeWalkerSourceInformation.Builder(lambdaId, 0, 0).build();
        String prefix = "function go():Any[*]{";
        String fullCode = prefix + code + "}";
        ParseTreeWalkerSourceInformation walkerSourceInformation = new ParseTreeWalkerSourceInformation.Builder(lambdaWalkerSourceInformation)
                // NOTE: as we prepend the lambda with this prefix, we need to subtract this prefix length from the column offset
                .withColumnOffset(lambdaWalkerSourceInformation.getColumnOffset() - prefix.length()).build();
        SourceCodeParserInfo sectionParserInfo = this.getParserInfo(fullCode, null, walkerSourceInformation, true);
        DomainParseTreeWalker walker = new DomainParseTreeWalker(walkerSourceInformation, new PureGrammarParserContext(), (ImportAwareCodeSection) null);
        return (Lambda) walker.concreteFunctionDefinition(((DomainParserGrammar.DefinitionContext) sectionParserInfo.rootContext).elementDefinition(0).functionDefinition());
    }

    // FIXME: to remove walker context when we remove inference for flat-data column type
    public ValueSpecification parseCombinedExpression(String code, ParseTreeWalkerSourceInformation combinedExpressionWalkerSourceInformation, PureGrammarParserContext parserContext, String flatDataRecordTypeSource)
    {
        List<String> typeParametersNames = new ArrayList<>();
        DomainParseTreeWalker.LambdaContext lambdaContext = new DomainParseTreeWalker.LambdaContext("");
        ParseTreeWalkerSourceInformation walkerSourceInformation = new ParseTreeWalkerSourceInformation.Builder(combinedExpressionWalkerSourceInformation).build();
        SourceCodeParserInfo sectionParserInfo = this.getParserInfo(code, null, walkerSourceInformation, false);
        DomainParseTreeWalker walker = new DomainParseTreeWalker(walkerSourceInformation, parserContext, flatDataRecordTypeSource);
        return walker.combinedExpression(((DomainParserGrammar) sectionParserInfo.parser).combinedExpression(), "line", typeParametersNames, lambdaContext, "", true, false);
    }
}
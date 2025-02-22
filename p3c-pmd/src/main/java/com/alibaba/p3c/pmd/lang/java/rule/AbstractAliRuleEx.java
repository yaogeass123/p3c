/*
 * Copyright 1999-2017 Alibaba Group.
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
package com.alibaba.p3c.pmd.lang.java.rule;

import com.alibaba.p3c.pmd.I18nResourcesEx;
import com.alibaba.p3c.pmd.fix.FixClassTypeResolver;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import org.apache.commons.lang3.StringUtils;

/**
 * re calculate node type
 *
 * @author caikang
 * @date 2016/11/20
 */
public abstract class AbstractAliRuleEx extends AbstractJavaRule {

    private static final Map<String, Boolean> TYPE_RESOLVER_MAP = new ConcurrentHashMap<>(16);

    private static final String EMPTY_FILE_NAME = "n/a";
    private static final String DELIMITER = "-";

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        // Each CompilationUnit will be scanned only once by custom type resolver.
        String sourceCodeFilename = ((RuleContext)data).getSourceCodeFilename();

        // Do type resolve if file name is empty(unit tests).
        if (StringUtils.isBlank(sourceCodeFilename) || EMPTY_FILE_NAME.equals(sourceCodeFilename)) {
            resolveType(node, data);
            return super.visit(node, data);
        }

        // If file name is not empty, use filename + hashcode to identify a compilation unit.
        String uniqueId = sourceCodeFilename + DELIMITER + node.hashCode();
        if (!TYPE_RESOLVER_MAP.containsKey(uniqueId)) {
            resolveType(node, data);
            TYPE_RESOLVER_MAP.put(uniqueId, true);
        }
        return super.visit(node, data);
    }

    @Override
    public void setDescription(String description) {
        super.setDescription(I18nResourcesEx.getMessage(description));
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(I18nResourcesEx.getMessageWithExceptionHandled(message));
    }

    @Override
    public void addViolationWithMessage(Object data, Node node, String message) {
        super.addViolationWithMessage(data, node,
                I18nResourcesEx.getMessageWithExceptionHandled(message));
    }

    @Override
    public void addViolationWithMessage(Object data, Node node, String message, Object[] args) {
        super.addViolationWithMessage(data, node,
                String.format(I18nResourcesEx.getMessageWithExceptionHandled(message), args));
    }

    private void resolveType(ASTCompilationUnit node, Object data) {
        FixClassTypeResolver classTypeResolver = new FixClassTypeResolver(
                AbstractAliRuleEx.class.getClassLoader());
        node.setClassTypeResolver(classTypeResolver);
        node.jjtAccept(classTypeResolver, data);
    }
}


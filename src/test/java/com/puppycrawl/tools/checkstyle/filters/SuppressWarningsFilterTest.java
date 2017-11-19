////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2017 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.filters;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.Test;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.checks.SuppressWarningsHolder;
import com.puppycrawl.tools.checkstyle.checks.UncommentedMainCheck;
import com.puppycrawl.tools.checkstyle.checks.coding.IllegalCatchCheck;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocTypeCheck;
import com.puppycrawl.tools.checkstyle.checks.naming.ConstantNameCheck;
import com.puppycrawl.tools.checkstyle.checks.naming.MemberNameCheck;
import com.puppycrawl.tools.checkstyle.checks.sizes.ParameterNumberCheck;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

public class SuppressWarningsFilterTest
    extends AbstractModuleTestSupport {
    private static final String[] ALL_MESSAGES = {
        "16: Missing a Javadoc comment.",
        "17: Missing a Javadoc comment.",
        "19: Missing a Javadoc comment.",
        "22:45: Name 'I' must match pattern '^[a-z][a-zA-Z0-9]*$'.",
        "24:17: Name 'J' must match pattern '^[a-z][a-zA-Z0-9]*$'.",
        "25:17: Name 'K' must match pattern '^[a-z][a-zA-Z0-9]*$'.",
        "29:17: Name 'L' must match pattern '^[a-z][a-zA-Z0-9]*$'.",
        "29:32: Name 'X' must match pattern '^[a-z][a-zA-Z0-9]*$'.",
        "33:30: Name 'm' must match pattern '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.",
        "34:30: Name 'n' must match pattern '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.",
        "39:17: More than 7 parameters (found 8).",
        "45:9: Catching 'Exception' is not allowed.",
        "56:9: Catching 'Exception' is not allowed.",
        "61: Missing a Javadoc comment.",
        "71: Uncommented main method found.",
        "76: Missing a Javadoc comment.",
        "77: Uncommented main method found.",
        "83: Missing a Javadoc comment.",
        "84: Uncommented main method found.",
        "90: Missing a Javadoc comment.",
        "91: Uncommented main method found.",
        "97: Missing a Javadoc comment.",
    };

    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/filters/suppresswarningsfilter";
    }

    @Test
    public void testNone() throws Exception {
        final DefaultConfiguration filterConfig = null;
        final String[] suppressed = CommonUtils.EMPTY_STRING_ARRAY;
        verifySuppressed(filterConfig, suppressed);
    }

    @Test
    public void testDefault() throws Exception {
        final DefaultConfiguration filterConfig =
            createModuleConfig(SuppressWarningsFilter.class);
        final String[] suppressed = {
            "24:17: Name 'J' must match pattern '^[a-z][a-zA-Z0-9]*$'.",
            "29:17: Name 'L' must match pattern '^[a-z][a-zA-Z0-9]*$'.",
            "33:30: Name 'm' must match pattern '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.",
            "39:17: More than 7 parameters (found 8).",
            "56:9: Catching 'Exception' is not allowed.",
            "71: Uncommented main method found.",
            "77: Uncommented main method found.",
            "84: Uncommented main method found.",
            "91: Uncommented main method found.",
            "97: Missing a Javadoc comment.",
        };
        verifySuppressed(filterConfig, suppressed);
    }

    private void verifySuppressed(Configuration moduleConfig,
            String... aSuppressed)
            throws Exception {
        verifySuppressed(moduleConfig, getPath("InputSuppressWarningsFilter.java"),
               ALL_MESSAGES, aSuppressed);
    }

    private void verifySuppressed(Configuration moduleConfig, String fileName,
            String[] expectedViolations, String... suppressedViolations) throws Exception {
        final DefaultConfiguration holderConfig =
            createModuleConfig(SuppressWarningsHolder.class);
        holderConfig.addAttribute("aliasList",
            "com.puppycrawl.tools.checkstyle.checks.sizes."
                + "ParameterNumberCheck=paramnum");

        final DefaultConfiguration memberNameCheckConfig =
                createModuleConfig(MemberNameCheck.class);
        memberNameCheckConfig.addAttribute("id", "ignore");

        final DefaultConfiguration constantNameCheckConfig =
            createModuleConfig(ConstantNameCheck.class);
        constantNameCheckConfig.addAttribute("id", "");

        final DefaultConfiguration uncommentedMainCheckConfig =
            createModuleConfig(UncommentedMainCheck.class);
        uncommentedMainCheckConfig.addAttribute("id", "ignore");

        final DefaultConfiguration treewalkerConfig =
                createModuleConfig(TreeWalker.class);
        treewalkerConfig.addChild(holderConfig);
        treewalkerConfig.addChild(memberNameCheckConfig);
        treewalkerConfig.addChild(constantNameCheckConfig);
        treewalkerConfig.addChild(createModuleConfig(ParameterNumberCheck.class));
        treewalkerConfig.addChild(createModuleConfig(IllegalCatchCheck.class));
        treewalkerConfig.addChild(uncommentedMainCheckConfig);
        treewalkerConfig.addChild(createModuleConfig(JavadocTypeCheck.class));

        final DefaultConfiguration checkerConfig =
                createRootConfig(treewalkerConfig);
        if (moduleConfig != null) {
            checkerConfig.addChild(moduleConfig);
        }

        verify(checkerConfig,
                fileName,
            removeSuppressed(expectedViolations, suppressedViolations));
    }

    private static String[] removeSuppressed(String[] from, String... remove) {
        final Collection<String> coll = Arrays.stream(from).collect(Collectors.toList());
        coll.removeAll(Arrays.asList(remove));
        return coll.toArray(new String[coll.size()]);
    }

    @Test
    public void testSuppressById() throws Exception {
        final DefaultConfiguration filterConfig =
            createModuleConfig(SuppressWarningsFilter.class);
        final String[] suppressedViolationMessages = {
            "6:17: Name 'A1' must match pattern '^[a-z][a-zA-Z0-9]*$'.",
            "8: Uncommented main method found.",
        };
        final String[] expectedViolationMessages = {
            "3: Missing a Javadoc comment.",
            "6:17: Name 'A1' must match pattern '^[a-z][a-zA-Z0-9]*$'.",
            "8: Uncommented main method found.",
        };

        verifySuppressed(filterConfig, getPath("InputSuppressWarningsFilterById.java"),
                expectedViolationMessages, suppressedViolationMessages);
    }
}

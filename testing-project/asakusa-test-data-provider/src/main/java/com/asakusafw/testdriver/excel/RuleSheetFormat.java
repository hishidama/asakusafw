/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.testdriver.excel;

/**
 * Format of verification rule description on Excel Sheet.
 * @since 0.7.0
 */
public enum RuleSheetFormat {

    /**
     * Title of the sheet format.
     * This title must be "Format", it is used for detect the format of each sheet.
     * The format kind must be set on the immediate right cell.
     */
    FORMAT("Format", 0, 0), //$NON-NLS-1$

    /**
     * Title of the total condition.
     * The total condition kind must be set on the immediate right cell,
     * and it must be one of {@link TotalConditionKind#getOptions()}.
     */
    TOTAL_CONDITION(Messages.getString("RuleSheetFormat.titleTotalCondition"), 1, 0), //$NON-NLS-1$

    /**
     * Title of property names.
     * Each property name must be set on the same column below this cell,
     * and it must be form of "snake_case".
     */
    PROPERTY_NAME(Messages.getString("RuleSheetFormat.titlePropertyName"), 2, 0), //$NON-NLS-1$

    /**
     * Title of the condition of property's value.
     * Each item must be set on the same column below this cell,
     * and it must be one of {@link ValueConditionKind#getOptions()}.
     */
    VALUE_CONDITION(Messages.getString("RuleSheetFormat.titleValueCondition"), 2, 1), //$NON-NLS-1$

    /**
     * Title of the condition of property's nullity.
     * Each item must be set on the same column below this cell,
     * and it must be one of {@link NullityConditionKind#getOptions()}.
     */
    NULLITY_CONDITION(Messages.getString("RuleSheetFormat.titleNullityCondition"), 2, 2), //$NON-NLS-1$

    /**
     * Title of comments of property.
     * Each item must be set on the same column below this cell.
     */
    COMMENTS(Messages.getString("RuleSheetFormat.titleComments"), 2, 3), //$NON-NLS-1$

    /**
     * Title of options of property.
     * Each item must be set on the same column below this cell.
     */
    EXTRA_OPTIONS(Messages.getString("RuleSheetFormat.titleOption"), 2, 4), //$NON-NLS-1$
    ;

    /**
     * Format ID which this extractor supports.
     * This must be set on the right cell of {@link RuleSheetFormat#FORMAT}.
     */
    public static final String FORMAT_VERSION = "EVR-2.0.0"; //$NON-NLS-1$

    private final String title;

    private final int rowIndex;

    private final int columnIndex;

    RuleSheetFormat(String title, int rowIndex, int columnIndex) {
        assert title != null;
        this.title = title;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
    }

    /**
     * Returns the title of this kind.
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the row index where this kind assigned in a sheet.
     * @return the row index (0-origin)
     */
    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * Returns the column index where this kind assigned in a sheet.
     * @return the column index (0-origin)
     */
    public int getColumnIndex() {
        return columnIndex;
    }
}

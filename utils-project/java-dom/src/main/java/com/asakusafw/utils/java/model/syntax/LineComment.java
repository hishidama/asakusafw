/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.utils.java.model.syntax;


/**
 * 行コメントを表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:3.7] Comments (<i>EndOfLineComment</i>)} </li>
 *   </ul> </li>
 * </ul>
 */
public interface LineComment
        extends Comment {

    // properties

    /**
     * コメント文字列を返す。
     * <p> コメント文字列には行コメントの区切り子<code>&#47&#47;</code>が含まれる。 </p>
     * @return
     *     コメント文字列
     */
    String getString();
}
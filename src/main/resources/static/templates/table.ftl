<#import "base/common.ftl" as c>
<@c.page>
    <label>C ${fromDate?date("yyyyMMdd")} были загружены данные с ${count} дистр.</label>
    <table class="table table-bordered table-hover" id="distributor-list">
        <thead class="thead-dark">
        <tr>
            <th>№</th>
            <#list header?values as header>
                <th class="text-center">${header}</th>
            </#list>
        </tr>
        </thead>
        <tbody>

        <#list elements as elements>
            <tr>
                <td align="center">${elements_index+1}</td>
                <td align="center">${elements.nameOfDistr}</td>
                <td align="center">${elements.nodeId}</td>
                <td align="center">${elements.distrId?c}</td>
                <#if elements.dateOfChange?has_content>
                    <td align="center">${elements.dateOfChange?datetime?string("yyyy-MM-dd hh:mm")}</td>
                <#else>
                    <td align="center"></td>
                </#if>
                <td align="center">${elements.protocol}</td>
                <td align="center">${elements.status}</td>

                <#if elements.firstSession?has_content>
                    <td align="center">${elements.firstSession?datetime?string("yyyy-MM-dd hh:mm")}</td>
                <#else>
                    <td align="center"></td>
                </#if>

                <#if elements.lastSession?has_content>
                    <td align="center">${elements.lastSession?datetime?string("yyyy-MM-dd hh:mm")}</td>
                <#else>
                    <td align="center"></td>
                </#if>
            </tr>
        </#list>
        </tbody>
    </table>
</@c.page>

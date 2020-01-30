<#import "base/common.ftl" as c>
<@c.page>
    <label>C ${fromDate?date("yyyyMMdd")} была включена загрузка по ${countEnabled} дистр. Данные загружались
        по ${hasSessions} дистр.</label>
    <table class="table table-bordered table-hover" id="distributor-list">
        <thead class="thead-dark">
        <tr>
            <th>№</th>
            <#list header?keys as header>
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
                <td align="center">${elements.dateOfChangeString()}</td>
                <td align="center">${elements.protocol}</td>
                <td align="center">${elements.status}</td>
                <td align="center">${elements.firstSessionString()}</td>
                <td align="center">${elements.lastSessionString()}</td>

            </tr>
        </#list>
        </tbody>
    </table>
</@c.page>

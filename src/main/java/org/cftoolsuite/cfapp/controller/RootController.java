package org.cftoolsuite.cfapp.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class RootController {

    @GetMapping(value = { "/", "" }, produces = MediaType.TEXT_HTML_VALUE)
    public Mono<ResponseEntity<String>> index() {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>cf-butler API</title>
            <style>
              body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 900px; margin: 40px auto; padding: 0 20px; color: #333; line-height: 1.6; }
              h1 { border-bottom: 2px solid #eee; padding-bottom: 10px; }
              h2 { color: #555; margin-top: 30px; }
              table { width: 100%%; border-collapse: collapse; margin: 10px 0 20px; }
              th, td { text-align: left; padding: 8px 12px; border-bottom: 1px solid #eee; }
              th { background: #f5f5f5; font-weight: 600; }
              code { background: #f5f5f5; padding: 2px 6px; border-radius: 3px; font-size: 0.9em; }
              .method { font-weight: 600; display: inline-block; width: 50px; }
              .method-GET { color: #2e7d32; }
              .method-POST { color: #1565c0; }
              .method-DELETE { color: #c62828; }
              .method-PATCH { color: #f57f17; }
              .note { font-size: 0.85em; color: #888; }
              .footer { margin-top: 40px; border-top: 1px solid #eee; padding-top: 10px; font-size: 0.85em; color: #888; }
            </style>
            </head>
            <body>
            <h1>cf-butler API</h1>
            <p>Cloud Foundry butler — snapshot, account, and policy management.</p>

            <h2>Snapshot</h2>
            <table>
              <tr><th>Method</th><th>Path</th><th>Description</th></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/detail</code></td><td>Full snapshot detail</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/detail/ai</code></td><td>AI application detail (CSV)</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/detail/ai/spring</code></td><td>Spring AI application detail</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/summary/ai/spring</code></td><td>Spring AI summary</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/detail/relations</code></td><td>Application relationships (CSV)</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/detail/si</code></td><td>Service instances (CSV)</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/detail/dormant/{days}</code></td><td>Dormant workloads</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/detail/legacy</code></td><td>Legacy workloads</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/detail/users</code></td><td>User accounts (CSV)</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/summary</code></td><td>Snapshot summary</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/demographics</code></td><td>Demographics</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/organizations</code></td><td>Organizations list</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/organizations/count</code></td><td>Organization count</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/users</code></td><td>Users list</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/users/count</code></td><td>User count</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/spaces</code></td><td>Spaces list</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/spaces/count</code></td><td>Space count</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/spaces/users</code></td><td>Space users</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/spaces/users/{name}</code></td><td>User's spaces</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/snapshot/{org}/{space}/users</code></td><td>Org/space users</td></tr>
            </table>

            <h2>Policies</h2>
            <table>
              <tr><th>Method</th><th>Path</th><th>Description</th></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/policies</code></td><td>List all policies</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/policies/application/{id}</code></td><td>Get application policy</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/policies/endpoint/{id}</code></td><td>Get endpoint policy</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/policies/hygiene/{id}</code></td><td>Get hygiene policy</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/policies/legacy/{id}</code></td><td>Get legacy policy</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/policies/query/{id}</code></td><td>Get query policy</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/policies/serviceInstance/{id}</code></td><td>Get service instance policy</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/policies/report</code></td><td>Historical policy report</td></tr>
              <tr><td><span class="method method-POST">POST</span></td><td><code>/policies/refresh</code></td><td>Reload policies from source</td></tr>
            </table>

            <h2>Policies — DBMS CRUD</h2>
            <table>
              <tr><th>Method</th><th>Path</th><th>Description</th></tr>
              <tr><td><span class="method method-POST">POST</span></td><td><code>/policies</code></td><td>Create policy</td></tr>
              <tr><td><span class="method method-DELETE">DELETE</span></td><td><code>/policies</code></td><td>Delete all policies</td></tr>
              <tr><td><span class="method method-DELETE">DELETE</span></td><td><code>/policies/application/{id}</code></td><td>Delete application policy</td></tr>
              <tr><td><span class="method method-DELETE">DELETE</span></td><td><code>/policies/endpoint/{id}</code></td><td>Delete endpoint policy</td></tr>
              <tr><td><span class="method method-DELETE">DELETE</span></td><td><code>/policies/hygiene/{id}</code></td><td>Delete hygiene policy</td></tr>
              <tr><td><span class="method method-DELETE">DELETE</span></td><td><code>/policies/legacy/{id}</code></td><td>Delete legacy policy</td></tr>
              <tr><td><span class="method method-DELETE">DELETE</span></td><td><code>/policies/query/{id}</code></td><td>Delete query policy</td></tr>
              <tr><td><span class="method method-DELETE">DELETE</span></td><td><code>/policies/resourcenotification/{id}</code></td><td>Delete resource notification policy</td></tr>
              <tr><td><span class="method method-DELETE">DELETE</span></td><td><code>/policies/serviceInstance/{id}</code></td><td>Delete service instance policy</td></tr>
            </table>

            <h2>Collection</h2>
            <table>
              <tr><th>Method</th><th>Path</th><th>Description</th></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/collect</code></td><td>Trigger data collection</td></tr>
              <tr><td><span class="method method-POST">POST</span></td><td><code>/collect</code></td><td>On-demand collection</td></tr>
              <tr><td><span class="method method-POST">POST</span></td><td><code>/policies/execute</code></td><td>On-demand policy execution</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/events/{id}</code></td><td>On-demand event status</td></tr>
            </table>

            <h2>Accounting</h2>
            <table>
              <tr><th>Method</th><th>Path</th><th>Description</th></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/accounting/applications</code></td><td>All application usage</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/accounting/applications/{org}/{start}/{end}</code></td><td>Application usage by org &amp; date range</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/accounting/services</code></td><td>All service usage</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/accounting/services/{org}/{start}/{end}</code></td><td>Service usage by org &amp; date range</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/accounting/tasks</code></td><td>All task usage</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/accounting/tasks/{org}/{start}/{end}</code></td><td>Task usage by org &amp; date range</td></tr>
            </table>

            <h2>Ops Manager <span class="note">(if om.enabled=true)</span></h2>
            <table>
              <tr><th>Method</th><th>Path</th><th>Description</th></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/products/deployed</code></td><td>Deployed products</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/products/om/info</code></td><td>Ops Manager info</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/products/stemcell/assignments</code></td><td>Stemcell assignments</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/products/stemcell/associations</code></td><td>Stemcell associations</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/products/metrics</code></td><td>Product metrics</td></tr>
            </table>

            <h2>Pivnet <span class="note">(if pivnet.enabled=true)</span></h2>
            <table>
              <tr><th>Method</th><th>Path</th><th>Description</th></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/store/product/releases</code></td><td>Product releases</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/store/product/catalog</code></td><td>Product catalog</td></tr>
            </table>

            <h2>Metadata <span class="note">(if profile=on-demand)</span></h2>
            <table>
              <tr><th>Method</th><th>Path</th><th>Description</th></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/metadata/{type}</code></td><td>List resources by type</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/metadata/{type}/{id}</code></td><td>Get resource metadata</td></tr>
              <tr><td><span class="method method-PATCH">PATCH</span></td><td><code>/metadata/{type}/{id}</code></td><td>Update resource metadata</td></tr>
            </table>

            <h2>Download</h2>
            <table>
              <tr><th>Method</th><th>Path</th><th>Description</th></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/download/pomfiles</code></td><td>Download POM files</td></tr>
            </table>

            <h2>Actuator</h2>
            <table>
              <tr><th>Method</th><th>Path</th><th>Description</th></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/actuator/health</code></td><td>Health check</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/actuator/info</code></td><td>Application info</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/actuator/metrics</code></td><td>Metrics</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/actuator/loggers</code></td><td>Loggers</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/actuator/scheduledtasks</code></td><td>Scheduled tasks</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/actuator/prometheus</code></td><td>Prometheus metrics</td></tr>
              <tr><td><span class="method method-GET">GET</span></td><td><code>/actuator/sbom</code></td><td>SBOM</td></tr>
            </table>

            <div class="footer">
              cf-butler &mdash; <a href="https://github.com/cftoolsuite/cf-butler">github.com/cftoolsuite/cf-butler</a>
            </div>
            </body>
            </html>
            """;
        return Mono.just(ResponseEntity.ok().body(html));
    }

}

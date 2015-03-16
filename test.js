var port, server, service,
    system = require('system');
var page = require('webpage').create();
var url = require('url');
		if (system.args.length !== 2) {
    console.log('Usage: simpleserver.js <portnumber>');
    phantom.exit(1);
} else {
    port = system.args[1];
    server = require('webserver').create();

    service = server.listen(port, function (request, response) {

        console.log('Vishnu Request at ' + new Date());
        //console.log(JSON.stringify(request, null, 4));
        var url_parts = url.parse(request.url, true);
		var query = url_parts.query;
        console.log("Text is "+query.query);
        response.statusCode = 200;
        response.headers = {
            'Cache': 'no-cache',
            'Content-Type': 'text/html'
        };

        page.open('http://wikipedia-miner.cms.waikato.ac.nz/demos/annotate/?source='+encodeURIComponent(query.query)+'&sourceMode=AUTO&repeatMode=FIRST_IN_REGION&minProbability=0.5', function () {
		    console.log(page.content.length);
		    response.write(page.content);
		    response.close();
		    // phantom.exit();
		});
       
        
    });

    if (service) {
        console.log('Web server running on port ' + port);
    } else {
        console.log('Error: Could not create web server listening on port ' + port);
        phantom.exit();
    }
}


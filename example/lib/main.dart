import 'dart:developer';
import 'package:flutter/material.dart';
import 'package:jete_tws_sdk/jete_tws_sdk.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final tws = JeteTwsSdk();

  @override
  void initState() {
    tws.deviceInfoStream.listen((event) {
      log(event.toString(), name: "deviceInfoStream");
    });
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: ListView(
            shrinkWrap: true,
            children: [
              const Divider(
                height: 16,
              ),
              TextButton(
                  onPressed: tws.startScan, child: const Text("startScan")),
              const Divider(
                height: 16,
              ),
              TextButton(
                  onPressed: tws.stopScan, child: const Text("stopScan")),
              const Divider(
                height: 16,
              ),
              TextButton(
                  onPressed: tws.disconnect, child: const Text("disconnect")),
              const Divider(
                height: 16,
              ),
              TextButton(
                  onPressed: tws.deviceInfo, child: const Text("deviceInfo")),
              const Divider(
                height: 16,
              ),
              StreamBuilder<dynamic>(
                stream: tws.scannerResultStream,
                builder: (
                  BuildContext context,
                  AsyncSnapshot<dynamic> snapshot,
                ) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return const SizedBox();
                  } else if (snapshot.connectionState ==
                          ConnectionState.active ||
                      snapshot.connectionState == ConnectionState.done) {
                    if (snapshot.hasError) {
                      return const Text(
                        'Error',
                      );
                    } else if (snapshot.hasData) {
                      List listdata = snapshot.data ?? [];
                      return ListView.builder(
                          shrinkWrap: true,
                          itemCount: listdata.length,
                          itemBuilder: (context, i) {
                            return InkWell(
                              onTap: () async {
                                tws.stopScan();
                                tws.bondDevice(device: listdata[i]);
                              },
                              child: Padding(
                                padding: const EdgeInsets.only(
                                    left: 8, bottom: 24.0),
                                child: Row(
                                  crossAxisAlignment: CrossAxisAlignment.center,
                                  mainAxisAlignment: MainAxisAlignment.start,
                                  children: [
                                    const Icon(
                                      Icons.signal_cellular_alt,
                                      size: 24,
                                    ),
                                    const SizedBox(
                                      width: 12,
                                    ),
                                    Expanded(
                                      child: Column(
                                        crossAxisAlignment:
                                            CrossAxisAlignment.start,
                                        mainAxisAlignment:
                                            MainAxisAlignment.start,
                                        children: [
                                          Text(
                                            "${listdata[i]['deviceName']}"
                                                    .isEmpty
                                                ? 'no name'
                                                : "${listdata[i]['deviceName']}",
                                            style: const TextStyle(
                                                color: Colors.black,
                                                fontWeight: FontWeight.bold),
                                            textAlign: TextAlign.start,
                                          ),
                                          Text(
                                            "${listdata[i]['deviceMacAddress']}",
                                            style: const TextStyle(
                                                color: Colors.black,
                                                fontWeight: FontWeight.normal),
                                            textAlign: TextAlign.start,
                                          )
                                        ],
                                      ),
                                    )
                                  ],
                                ),
                              ),
                            );
                          });
                    } else {
                      return const Text(
                        'Empty Data',
                        textAlign: TextAlign.center,
                      );
                    }
                  } else {
                    return Text(
                      'State: ${snapshot.connectionState}',
                      textAlign: TextAlign.center,
                    );
                  }
                },
              )
            ],
          ),
        ),
      ),
    );
  }
}

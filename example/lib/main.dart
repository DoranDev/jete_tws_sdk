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
                  onPressed: tws.bondDevice, child: const Text("bondDevice")),
              const Divider(
                height: 16,
              ),
              TextButton(
                  onPressed: tws.disconnect, child: const Text("disconnect")),
              const Divider(
                height: 16,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

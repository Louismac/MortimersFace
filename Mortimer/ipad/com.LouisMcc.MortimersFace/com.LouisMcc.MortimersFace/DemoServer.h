//
//  DemoServer.h
//  F53OSC Monitor
//
//  Created by Adam Bachman on 8/5/15.
//  Copyright (c) 2015 Figure 53. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "F53OSC.h"
#import "BlockFn.h"

@class AppDelegate;

@interface DemoServer : NSObject <F53OSCPacketDestination>

- (id) initWithPort:(UInt16)port;

- (void) start;
- (void) stop;
- (bool) isActive;

- (NSString *)stats;
- (NSNumber *)bytesPerSecond;

BLOCK_PROPERTY StringFnBlock onReceived;

@end

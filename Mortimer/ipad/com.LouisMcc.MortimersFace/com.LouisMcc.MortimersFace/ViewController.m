//
//  ViewController.m
//  com.LouisMcc.MortimersFace
//
//  Created by LouisMcCallum on 18/12/2016.
//  Copyright © 2016 LouisMcCallum. All rights reserved.
//

#import "ViewController.h"
#import "BlockFn.h"

@interface ViewController ()

@property(nonatomic, strong) NSMutableDictionary *images;
@property(nonatomic, strong) UIImage *face;
@property(nonatomic, strong) UIImageView *faceView;
@property(nonatomic, strong) UIImageView *mouthView;
@property(nonatomic, strong) UIImageView *eyelidLView;
@property(nonatomic, strong) UIImageView *eyelidRView;
@property(nonatomic, strong) UIImageView *eyebrowLView;
@property(nonatomic, strong) UIImageView *eyebrowRView;
@property (nonatomic,strong) F53OSCServer *server;
@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    if(!self.images) {
        self.images = [NSMutableDictionary new];
        
        [self loadImagesWithRoot:@"eyelids" withAmount:4 leftAndRight:YES];
        [self loadImagesWithRoot:@"eyebrows" withAmount:8 leftAndRight:YES];
        [self loadImagesWithRoot:@"mouth" withAmount:8 leftAndRight:NO];
        
        self.server = [F53OSCServer new];
        self.server.delegate = self;
        self.server.port = 23456;
        [self.server startListening];
    }
}

-(void) loadImagesWithRoot:(NSString *) root
                withAmount:(NSUInteger) amount
              leftAndRight:(BOOL) leftAndRight
{
    for(NSUInteger i=0;i<amount;i++) {
        [self loadImageWithRoot:root withIndex:i leftAndRight:leftAndRight];
    }
}

-(void) loadImageWithRoot:(NSString *) root
                withIndex:(NSUInteger) index
              leftAndRight:(BOOL) leftAndRight
{
    NSLog(@"Loading %@ %lu",root,(unsigned long)index);
    if(leftAndRight) {
        NSString *left = [NSString stringWithFormat:@"%@%lu_l",root,(unsigned long)index];
        NSString *right = [NSString stringWithFormat:@"%@%lu_r",root,(unsigned long)index];
        self.images[left] = [UIImage imageNamed:[NSString stringWithFormat:@"%@.png",left]];
        self.images[right] = [UIImage imageNamed:[NSString stringWithFormat:@"%@.png",right]];
    } else {
        NSString *key = [NSString stringWithFormat:@"%@%lu",root,(unsigned long)index];
        self.images[key] = [UIImage imageNamed:[NSString stringWithFormat:@"%@.png",key]];
    }
}

-(BOOL)prefersStatusBarHidden{
    return YES;
}

-(void) viewDidLayoutSubviews
{
    if(!self.face) {
        self.face = [UIImage imageNamed:@"face2.png"];
        self.faceView = [[UIImageView alloc] initWithImage:self.face];
        [self.view addSubview:self.faceView];
        self.faceView.frame = self.view.bounds;
        
        self.mouthView = [[UIImageView alloc] initWithImage:self.images[@"mouth0"]];
        [self.view addSubview:self.mouthView];
        self.mouthView.frame = self.view.bounds;
        
        self.eyelidLView = [[UIImageView alloc] initWithImage:self.images[@"eyelids0_l"]];
        [self.view addSubview:self.eyelidLView];
        self.eyelidLView.frame = self.view.bounds;
        
        self.eyelidRView = [[UIImageView alloc] initWithImage:self.images[@"eyelids0_r"]];
        [self.view addSubview:self.eyelidRView];
        self.eyelidRView.frame = self.view.bounds;
        
        self.eyebrowLView = [[UIImageView alloc] initWithImage:self.images[@"eyebrows0_l"]];
        [self.view addSubview:self.eyebrowLView];
        self.eyebrowLView.frame = self.view.bounds;
        
        self.eyebrowRView = [[UIImageView alloc] initWithImage:self.images[@"eyebrows0_r"]];
        [self.view addSubview:self.eyebrowRView];
        self.eyebrowRView.frame = self.view.bounds;
    }
}

- (void)takeMessage:(F53OSCMessage *)message
{
    // handle all messages synchronously
    [self performSelectorOnMainThread:@selector( _processMessage: ) withObject:message waitUntilDone:NO];
}

- (void)_processMessage:(F53OSCMessage *)message
{
    // log all received messages
    // build log string
    if ([message.arguments count] > 0) {
        onMainThread(^{
            [self setFromArg:message.arguments[0] andIndex:message.arguments[1]];
        });
        
    }
    
}

-(void)setFromArg:(NSString *) arg andIndex:(NSString *) index
{
    NSLog(@"setting %@ to %@",arg,index);
    if([arg isEqualToString:@"eyebrow_l"]) {
        
        NSString *key = [NSString stringWithFormat:@"eyebrows%@_l",index];
        self.eyebrowLView.image = self.images[key];
    } else if([arg isEqualToString:@"eyebrow_r"]) {
        NSString *key = [NSString stringWithFormat:@"eyebrows%@_r",index];
        self.eyebrowRView.image = self.images[key];
    } else if([arg isEqualToString:@"eyelid_l"]) {
        NSString *key = [NSString stringWithFormat:@"eyelids%@_l",index];
        self.eyelidLView.image = self.images[key];
    } else if([arg isEqualToString:@"eyelid_r"]) {
        NSString *key = [NSString stringWithFormat:@"eyelids%@_r",index];
        self.eyelidRView.image = self.images[key];
    } else if([arg isEqualToString:@"mouth"]) {
        NSString *key = [NSString stringWithFormat:@"mouth%@",index];
        self.mouthView.image = self.images[key];
    }
}

- (void)viewWillTransitionToSize:(CGSize)size withTransitionCoordinator:(id <UIViewControllerTransitionCoordinator>)coordinator
{
    [super viewWillTransitionToSize:size withTransitionCoordinator:coordinator];
    
    [coordinator animateAlongsideTransition:^(id<UIViewControllerTransitionCoordinatorContext> context) {
        
        CGFloat time = context.transitionDuration;
        [UIView animateWithDuration:time animations:^{
            
            self.faceView.frame = self.view.bounds;
            self.mouthView.frame = self.view.bounds;
            self.eyelidLView.frame = self.view.bounds;
            self.eyelidRView.frame = self.view.bounds;
            self.eyebrowLView.frame = self.view.bounds;
            self.eyebrowRView.frame = self.view.bounds;
        }];
        
        
    } completion:^(id<UIViewControllerTransitionCoordinatorContext> context) {
        
        self.faceView.frame = self.view.bounds;
        self.mouthView.frame = self.view.bounds;
        self.eyelidLView.frame = self.view.bounds;
        self.eyelidRView.frame = self.view.bounds;
        self.eyebrowLView.frame = self.view.bounds;
        self.eyebrowRView.frame = self.view.bounds;
        
    }];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
